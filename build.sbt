import scala.util.Properties.isJavaAtLeast

import Dependencies._

// Settings
ThisBuild / organization := "org.eu.acolyte"

// JDBC
val jdbcDriver = JdbcDriver.project

val jdbcJava8 = new JdbcJava8(jdbcDriver).project

val jdbcScala = new JdbcScala(jdbcDriver).project

val playJdbc = new PlayJdbc(jdbcScala).project

// ReactiveMongo
val rm = new ReactiveMongo()
val reactiveMongo = rm.project
val playReactiveMongo = rm.playProject

lazy val studio = (sbt.project in file("studio")).settings(
  autoScalaLibrary := false,
  crossPaths := false,
  resolvers += "Tatami Releases".at(
    "https://raw.github.com/cchantep/tatami/master/releases"
  ),
  libraryDependencies ++= Seq(
    "melasse" % "melasse-core" % "1.0",
    "de.sciss" % "syntaxpane" % "1.3.0",
    "org.apache.commons" % "commons-lang3" % "3.20.0"
  ),
  assembly / mainClass := Some("acolyte.Studio"),
  assembly / assemblyJarName := s"acolyte-studio-${version.value}.jar"
)

// Aggregation
val versionVariant = if (isJavaAtLeast("1.7")) "-j7p" else ""

val javaVersion =
  if (isJavaAtLeast("1.8")) "1.8"
  else if (isJavaAtLeast("1.7")) "1.7"
  else "1.6"

lazy val root = Project(id = "acolyte", base = file("."))
  .settings(Publish.settings)
  .aggregate(reactiveMongo, jdbcDriver, jdbcScala, studio)
  .disablePlugins(HighlightExtractorPlugin, ScaladocExtractorPlugin)
  .configure { p =>
    if (isJavaAtLeast("1.8")) {
      p.aggregate(playJdbc, jdbcJava8, playReactiveMongo)
    } else p
  }
