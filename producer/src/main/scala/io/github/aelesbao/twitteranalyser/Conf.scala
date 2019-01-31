package io.github.aelesbao.twitteranalyser

import java.util.Properties

import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._

case class Conf(twitter: TwitterConf, kafka: KafkaConf)

case class TwitterConf(terms: Seq[String])

case class KafkaConf(topic: String, producerProperties: Properties)

object Conf {

  implicit object PropertiesReader extends ConfigReader[Properties] {
    def from(cur: ConfigCursor): Either[ConfigReaderFailures, Properties] =
      cur.asObjectCursor
        .map(_.value.toConfig.entrySet())
        .map(entries => new Properties {
          entries.forEach(e => put(e.getKey, e.getValue.unwrapped().toString))
        })
  }

  private lazy val config: Conf = pureconfig.loadConfigOrThrow[Conf]

  def twitter: TwitterConf = config.twitter

  def kafka: KafkaConf = config.kafka
}
