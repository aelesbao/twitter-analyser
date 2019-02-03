package io.github.aelesbao

import java.util.Properties

import pureconfig.{ConfigCursor, ConfigReader}
import pureconfig.error.ConfigReaderFailures

package object twitteranalyser {

  implicit object PropertiesReader extends ConfigReader[Properties] {
    def from(cur: ConfigCursor): Either[ConfigReaderFailures, Properties] =
      cur.asObjectCursor
        .map(_.value.toConfig.entrySet())
        .map(entries => new Properties {
          entries.forEach(e => put(e.getKey, e.getValue.unwrapped().toString))
        })
  }

}
