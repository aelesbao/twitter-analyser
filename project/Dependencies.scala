import sbt._

object Version {
  val log4j = "2.11.1"
  val slf4j = "1.7.25"
  val scalaLogging = "3.9.2"
}

object Dependencies {
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
}
