package io.github.aelesbao.twitteranalyser

import java.util.Properties

import pureconfig.generic.auto._

case class ConsumerConfig(elasticsearch: ElasticSearchConfig, kafka: KafkaConfig)

case class ElasticSearchConfig(endpoint: String, username: String, password: String,
                               indexName: String)

case class KafkaConfig(topics: Map[String, String], consumerProperties: Properties)

object ConsumerConfig {

  private lazy val config = pureconfig.loadConfigOrThrow[ConsumerConfig]

  def elasticsearch: ElasticSearchConfig = config.elasticsearch

  def kafka: KafkaConfig = config.kafka
}
