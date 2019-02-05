package io.github.aelesbao.twitteranalyser

import java.util.Properties

import pureconfig.generic.auto._

case class ProducerConfig(twitter: TwitterConfig, kafka: KafkaConfig)

case class TwitterConfig(terms: Seq[String])

case class KafkaConfig(topics: Map[String, String], producerProperties: Properties)

object ProducerConfig {

  private lazy val config = pureconfig.loadConfigOrThrow[ProducerConfig]

  def twitter: TwitterConfig = config.twitter

  def kafka: KafkaConfig = config.kafka
}
