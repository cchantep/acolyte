import scala.util.Properties.isJavaAtLeast

import Dependencies._
import Format._
import ScalacPlugin._

// Settings
organization in ThisBuild := "org.eu.acolyte"

scalaVersion in ThisBuild := "2.12.10"

crossScalaVersions in ThisBuild := Seq(
  "2.10.7", "2.11.12", (scalaVersion in ThisBuild).value, "2.13.1"
)

resolvers in ThisBuild ++= Seq(
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
    "de.sciss" % "syntaxpane" % "1.2.0",
    "org.apache.commons" % "commons-lang3" % "3.10"),
  mainClass in assembly := Some("acolyte.Studio"),
  assemblyJarName in assembly := s"acolyte-studio-${version.value}.jar")

// Aggregation
val versionVariant = if (isJavaAtLeast("1.7")) "-j7p" else ""

val javaVersion =
  if (isJavaAtLeast("1.8")) "1.8"
  else if (isJavaAtLeast("1.7")) "1.7"
  else "1.6"

lazy val root = Project(id = "acolyte", base = file(".")).
  aggregate(scalacPlugin, reactiveMongo,
    jdbcDriver, jdbcScala, studio).
  disablePlugins(HighlightExtractorPlugin, ScaladocExtractorPlugin).
  settings(Publish.settings ++ Release.settings) configure { p =>
    if (isJavaAtLeast("1.8")) {
      p.aggregate(playJdbc, jdbcJava8, playReactiveMongo)
    }
    else p
  }
