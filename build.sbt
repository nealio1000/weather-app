version := "0.1.0-SNAPSHOT"
scalaVersion := "2.13.10"
organization := "org.neal"

lazy val circeVersion = "0.14.3"
lazy val scalatestVersion = "3.2.14"
lazy val scalatestMockitoVersion = "3.2.10.0"
lazy val scalaCheckVersion = "1.17.0"
lazy val jodaTimeVersion = "2.12.1"
lazy val scalaLoggingVersion = "3.9.5"
lazy val logbackVersion = "1.2.11"
lazy val logstashVersion = "7.1.1"
lazy val http4sVersion = "0.23.12"
lazy val swaggerUIVersion = "4.15.0"
lazy val webjarsLocatorVersion = "0.45"
lazy val circeJsonSchemaVersion = "0.2.0"
lazy val disciplineScalatestVersion = "2.2.0"

resolvers += "jitpack".at("https://jitpack.io")

lazy val deps = Seq(
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-literal" % circeVersion,
  "io.circe" %% "circe-json-schema" % circeJsonSchemaVersion,
  "joda-time" % "joda-time" % jodaTimeVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "net.logstash.logback" % "logstash-logback-encoder" % logstashVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-server" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % "it,test",
  "org.webjars" % "webjars-locator" % webjarsLocatorVersion,
  "org.webjars" % "swagger-ui" % swaggerUIVersion,
  "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test,
  "org.scalatestplus" %% "mockito-3-4" % scalatestMockitoVersion % Test,
  "io.circe" %% "circe-testing" % circeVersion % Test,
  "org.typelevel" %% "discipline-scalatest" % disciplineScalatestVersion % Test
)

lazy val weatherApp = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings,
    scalacOptions += "-deprecation",
    libraryDependencies ++= deps,
    assembly / mainClass := Some("WeatherServer"),
    assemblyMergeStrategy := {
      case PathList("META-INF", _ @_*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    },
    Test / parallelExecution := false
  )