import sbt._

object Version {
  val kafka = "2.1.0"
  val twitter4s = "5.5"
  val elastic4s = "6.5.1"
  val json4s = "3.6.4"
  val log4j = "2.11.1"
  val slf4j = "1.7.25"
  val scalaLogging = "3.9.2"
}

object Dependencies {
  val kafka: Seq[ModuleID] = Seq(
    "org.apache.kafka" % "kafka-clients" % Version.kafka
  )

  val twitter: Seq[ModuleID] = Seq(
    "com.danielasfregola" %% "twitter4s" % Version.twitter4s,
    "org.json4s" %% "json4s-native" % Version.json4s
  )

  val elasticsearch: Seq[ModuleID] = Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-core" % Version.elastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-http" % Version.elastic4s
  )

  val logging: Seq[ModuleID] = Seq(
    "org.apache.logging.log4j" % "log4j-api" % Version.log4j,
    "org.apache.logging.log4j" % "log4j-core" % Version.log4j,
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % Version.log4j,
    "org.slf4j" % "slf4j-api" % Version.slf4j,
    "com.typesafe.scala-logging" %% "scala-logging" % Version.scalaLogging
  )

  val config: Seq[ModuleID] = Seq(
    "com.typesafe" % "config" % "1.3.3",
    "com.github.pureconfig" %% "pureconfig" % "0.10.1"
  )

  val utilities: Seq[ModuleID] = Seq(
    "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.0"
  )
}
