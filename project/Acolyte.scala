import scala.util.Properties.isJavaAtLeast

import sbt._
import Keys._

// Multi-module project build
object Acolyte extends Build with Dependencies with Format 
    with ScalacPlugin with ReactiveMongo 
    with JdbcDriver with JdbcJava8 with JdbcScala with JdbcClojure
    with PlayJdbc with Studio {

  val versionVariant = if (isJavaAtLeast("1.7")) "-j7p" else ""

  val javaVersion =
    if (isJavaAtLeast("1.8")) "1.8"
    else if (isJavaAtLeast("1.7")) "1.7"
    else "1.6"

  lazy val root = Project(id = "acolyte", base = file(".")).
    aggregate(scalacPlugin, reactiveMongo,
      jdbcDriver, jdbcScala, jdbcClojure, studio).
    settings(Seq(
      organization in ThisBuild := "org.eu.acolyte",
      javaOptions in ThisBuild ++= Seq(
        "-source", javaVersion, "-target", javaVersion),
      scalaVersion in ThisBuild := "2.12.2",
      scalacOptions in ThisBuild ++= Seq(
        "-unchecked", "-deprecation", "-feature"),
      scalacOptions in ThisBuild ++= {
        val baseOpts = if (!scalaVersion.value.startsWith("2.10")) Seq(
          "-Ywarn-unused-import",
          //"-Xfatal-warnings",
          "-Xlint",
          "-Ywarn-numeric-widen",
          "-Ywarn-infer-any",
          "-Ywarn-dead-code",
          "-Ywarn-unused",
          "-Ywarn-unused-import",
          "-Ywarn-value-discard",
          "-g:vars"
        ) else Nil

        if (scalaVersion.value startsWith "2.11") baseOpts ++ Seq(
          "-Yconst-opt",
          "-Yclosure-elim",
          "-Ydead-code",
          "-Yopt:_"
        ) else baseOpts
      },
      crossScalaVersions in ThisBuild := Seq(
        "2.10.5", "2.11.11", (scalaVersion in ThisBuild).value
      ),
      publishTo in ThisBuild := Some(Resolver.file("file", 
        new File(Path.userHome.absolutePath+"/.m2/repository"))),
      homepage := Some(url("http://acolyte.eu.org")),
      licenses in ThisBuild := Seq(
        "GNU Lesser General Public License, Version 2.1" -> 
          url("https://raw.github.com/cchantep/acolyte/master/LICENSE.txt")),
      pomExtra in ThisBuild := 
      <url>http://acolyte.eu.org</url>
      <scm>
        <connection>scm:git:git@github.com:cchantep/acolyte.git</connection>
        <developerConnection>
          scm:git:git@github.com:cchantep/acolyte.git
        </developerConnection>
        <url>git@github.com:cchantep/acolyte.git</url>
      </scm>
      <issueManagement>
        <system>GitHub</system>
        <url>https://github.com/cchantep/acolyte/issues</url>
      </issueManagement>
      <ciManagement>
        <system>Travis CI</system>
        <url>https://travis-ci.org/cchantep/acolyte</url>
      </ciManagement>
      <developers>
        <developer>
          <id>cchantep</id>
          <name>Cedric Chantepie</name>
        </developer>
        </developers>) ++ Release.settings) configure { p =>
      if (isJavaAtLeast("1.8")) {
        p.aggregate(playJdbc, jdbcJava8, playReactiveMongo)
      }
      else p
    }
}

trait Dependencies {
  val specs2Test = "org.specs2" %% "specs2-core" % "3.9.4" % Test
}
