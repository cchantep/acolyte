import scala.util.Properties.isJavaAtLeast

import Dependencies._
import Format._
import ScalacPlugin._

// Settings
organization in ThisBuild := "org.eu.acolyte"

scalaVersion in ThisBuild := "2.12.8"

crossScalaVersions in ThisBuild := Seq(
  "2.10.7", "2.11.12", (scalaVersion in ThisBuild).value, "2.13.0"
)

resolvers in ThisBuild += Resolver.sonatypeRepo("snapshots")

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

//
val studio = Studio.project

// Aggregation
val versionVariant = if (isJavaAtLeast("1.7")) "-j7p" else ""

val javaVersion =
  if (isJavaAtLeast("1.8")) "1.8"
  else if (isJavaAtLeast("1.7")) "1.7"
  else "1.6"

lazy val root = Project(id = "acolyte", base = file(".")).
  aggregate(scalacPlugin, reactiveMongo,
    jdbcDriver, jdbcScala, studio).
  settings(Publish.settings ++ Release.settings) configure { p =>
    if (isJavaAtLeast("1.8")) {
      p.aggregate(playJdbc, jdbcJava8, playReactiveMongo)
    }
    else p
  }
