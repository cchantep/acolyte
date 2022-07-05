import scala.util.Properties.isJavaAtLeast

import Dependencies._
import Format._
import ScalacPlugin._

// Settings
ThisBuild / organization := "org.eu.acolyte"

ThisBuild / scalaVersion := "2.12.15"

ThisBuild / crossScalaVersions := Seq(
  "2.11.12", (ThisBuild / scalaVersion).value, "2.13.8"
)

ThisBuild / resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "Tatami Snapshots" at "https://raw.github.com/cchantep/tatami/master/snapshots")

//
val scalacPlugin = ScalacPlugin.project

// JDBC
val jdbcDriver = JdbcDriver.project

val jdbcJava8 = new JdbcJava8(jdbcDriver).project

val jdbcScala = new JdbcScala(jdbcDriver, scalacPlugin).project

val playJdbc = new PlayJdbc(scalacPlugin, jdbcScala).project

// ReactiveMongo
val rm = new ReactiveMongo(scalacPlugin)
val reactiveMongo = rm.project
val playReactiveMongo = rm.playProject

lazy val studio = (sbt.project in file("studio")).settings(
  autoScalaLibrary := false,
  crossPaths := false,
  resolvers += "Tatami Releases".at(
    "https://raw.github.com/cchantep/tatami/master/releases"),
  libraryDependencies ++= Seq(
    "melasse" % "melasse-core" % "1.0",
    "de.sciss" % "syntaxpane" % "1.2.1",
    "org.apache.commons" % "commons-lang3" % "3.12.0"),
  assembly / mainClass := Some("acolyte.Studio"),
  assembly / assemblyJarName := s"acolyte-studio-${version.value}.jar")

// Aggregation
val versionVariant = if (isJavaAtLeast("1.7")) "-j7p" else ""

val javaVersion =
  if (isJavaAtLeast("1.8")) "1.8"
  else if (isJavaAtLeast("1.7")) "1.7"
  else "1.6"

lazy val root = Project(id = "acolyte", base = file(".")).
  settings(Publish.settings).
  aggregate(scalacPlugin, reactiveMongo,
    jdbcDriver, jdbcScala, studio).
  disablePlugins(HighlightExtractorPlugin, ScaladocExtractorPlugin).
  configure { p =>
    if (isJavaAtLeast("1.8")) {
      p.aggregate(playJdbc, jdbcJava8, playReactiveMongo)
    }
    else p
  }
