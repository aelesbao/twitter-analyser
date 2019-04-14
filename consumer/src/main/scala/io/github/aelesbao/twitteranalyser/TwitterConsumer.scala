package io.github.aelesbao.twitteranalyser

import java.util

import com.sksamuel.elastic4s.http.{ElasticError, RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.indexes.IndexRequest
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}

import scala.collection.JavaConverters._
import scala.compat.java8.DurationConverters._
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.Try

object TwitterConsumer extends App {
  new TwitterConsumer().run()
}

class TwitterConsumer() extends Runnable with AutoCloseable with LazyLogging {
  val indexName: String = ConsumerConfig.elasticsearch.indexName
  val topic: String = ConsumerConfig.kafka.topics("tweets")

  private val client = ElasticClientBuilder.build()
  private val consumer =
    new KafkaConsumer[String, String](ConsumerConfig.kafka.consumerProperties) {
      subscribe(util.Arrays.asList(topic))
    }

  override def run(): Unit = {
    Try {
      Await.result(createTweetsIndex(), 5 seconds)
      consumeTweets()
    }.failed.foreach(logger.error("Failed to consume tweets", _))

    close()
  }

  private def createTweetsIndex(): Future[_] = {
    logger.info(s"Creating index '$indexName'")
    client.execute {
      createIndex(indexName)
        .indexSetting("mapping.total_fields.limit", 5000)
        .shards(3)
    } map {
      case RequestFailure(status, _, _, error) if resourceAlreadyExists(status, error) =>
        logger.warn("Index already exists")
      case RequestFailure(_, _, _, error) =>
        logger.error(s"Index '$indexName': $error")
        throw new RuntimeException(s"Could not create index '$indexName'")
      case _ => logger.info(s"Index created")
    }
  }

  private def resourceAlreadyExists(status: Int, error: ElasticError): Boolean =
    status == 400 && error.`type` == "resource_already_exists_exception"

  private def consumeTweets(): Unit =
    Stream.continually(consumer.poll(1.second.toJava).asScala)
      .foreach(indexRecords)

  private def indexRecords(records: Iterable[ConsumerRecord[String, String]]): Future[_] =
    if (records.nonEmpty) {
      logger.info(s"Indexing ${records.size} tweets in Elasticsearch")

      val recordsByPartition = records
        .groupBy(_.partition())
        .mapValues(_.size)
        .map { case (partition, size) => s"$partition=$size" }
      logger.debug(s"Records per partition: ${recordsByPartition.mkString(", ")}")

      client.execute {
        val requests = records.map(indexRecordRequest)
        bulk(requests)
      } map {
        case RequestSuccess(_, _, _, bulkResponse) =>
          logger.info(s"Indexed ${bulkResponse.successes.size} tweets successfully, " +
                        s"${bulkResponse.failures.size} failed")

          if (bulkResponse.hasFailures) {
            val failures = bulkResponse.failures
            logger.warn(s"No offsets will be committed. Please, check failures: $failures")
          }
          else {
            logger.debug("Committing offsets...")
            consumer.commitSync()
            logger.debug("Offsets have been committed")
          }

        case RequestFailure(status, _, _, error) =>
          logger.error(s"Bulk insert failed with status $status: $error")
      }
    }
    else Future.unit

  private def indexRecordRequest(record: ConsumerRecord[String, String]): IndexRequest =
    indexInto(indexName, "json")
      .id(record.key())
      .source(record.value())

  override def close(): Unit = {
    logger.info("Closing Kafka consumer")
    consumer.close()

    logger.info("Closing Elasticsearch client")
    client.close()
  }
}
