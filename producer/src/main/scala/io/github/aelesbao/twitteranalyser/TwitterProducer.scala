package io.github.aelesbao.twitteranalyser

import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.entities.streaming.CommonStreamingMessage
import com.danielasfregola.twitter4s.http.serializers.JsonSupport
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer._

class TwitterProducer extends Runnable with LazyLogging with JsonSupport {

  private val client = TwitterStreamingClient()
  private val terms = ProducerConfig.twitter.terms

  private val producer = new KafkaProducer[String, String](ProducerConfig.kafka.producerProperties)
  private val topic = ProducerConfig.kafka.topics("tweets")

  override def run(): Unit = {
    logger.info(s"Starting producer and streaming to $topic")
    client.filterStatuses(tracks = terms, stall_warnings = true)(processStatus)
  }

  private def processStatus: PartialFunction[CommonStreamingMessage, Unit] = {
    case tweet: Tweet => publish(tweet)
    case msg => logger.info(msg.toString)
  }

  private def publish(tweet: Tweet): Unit = {
    logger.info(s"Publishing tweet ${tweet.id}: ${tweet.text}")

    val tweetJson = serialization.write(tweet)
    val record = new ProducerRecord(topic, tweet.id_str, tweetJson)
    producer.send(record, (_: RecordMetadata, exception: Exception) => {
      if (exception != null) logger.error(s"Failed to publish tweet ${tweet.id}", exception)
    })
  }
}

/**
  * Before running this example, start Kafka using docker-compose up and then create the topic:
  *
  * kafka-topics --zookeeper $(dc port zookeeper 2181) \
  *   --create --topic streaming.twitter.tweets \
  *   --partitions 6 --replication-factor 2
  */
object TwitterProducer extends App {
  val twitterProducer = new TwitterProducer()
  twitterProducer.run()
}
