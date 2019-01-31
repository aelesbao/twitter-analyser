package io.github.aelesbao.twitteranalyser

import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.Tweet
import com.danielasfregola.twitter4s.http.serializers.JsonSupport
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer._

class TwitterProducer extends Runnable
  with LazyLogging
  with JsonSupport {

  private val client = TwitterStreamingClient()
  private val terms = Conf.twitter.terms

  private val producer = new KafkaProducer[String, String](Conf.kafka.producerProperties)
  private val topic = Conf.kafka.topic

  override def run(): Unit = {
    logger.info("Starting statuses filter")

    client.filterStatuses(tracks = terms, stall_warnings = true) {
      case tweet: Tweet => publish(tweet)
      case msg => logger.info(msg.toString)
    }
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

object TwitterProducer extends App {
  new TwitterProducer().run()
}
