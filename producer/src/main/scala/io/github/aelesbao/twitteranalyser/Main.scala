package io.github.aelesbao.twitteranalyser

import java.util.Properties

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.StringSerializer

/**
  * Before running this example, start Kafka using docker-compose up and then create the test topic:
  *
  * kafka-topics --zookeeper $(dc port zookeeper 2181) \
  *              --create --topic test_topic \
  *              --partitions 3 --replication-factor 1
  */
object Main extends App with LazyLogging {
  logger.info("Initializing producer")
  val properties = new Properties {
    put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "0.0.0.0:9092,0.0.0.0:9093")
    put(ProducerConfig.ACKS_CONFIG, "all")
    put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  }
  val producer = new KafkaProducer[String, String](properties)

  logger.info("Sending record")
  val record = new ProducerRecord[String, String]("test_topic", "key", "hello from the other side!")
  producer.send(record, (metadata: RecordMetadata, exception: Exception) => {
    if (exception != null)
      logger.error("Record send failed", exception)
    else
      logger.info(s"Record sent: $metadata")
  })

  logger.info("Flushing data and closing producer")
  producer.flush()
  producer.close()
}
