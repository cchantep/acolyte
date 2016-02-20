import scala.util.Properties.isJavaAtLeast

import sbt._
import Keys._

// Multi-module project build
object Acolyte extends Build with Dependencies 
    with ScalacPlugin with ReactiveMongo 
    with JdbcDriver with JdbcJava8 with JdbcScala with JdbcClojure with Studio {

  val versionVariant = if (isJavaAtLeast("1.7")) "-j7p" else ""

  val javaVersion =
    if (isJavaAtLeast("1.8")) "1.8"
    else if (isJavaAtLeast("1.7")) "1.7"
    else "1.6"

  lazy val root = Project(id = "acolyte", base = file(".")).
    aggregate(scalacPlugin, reactiveMongo,
      jdbcDriver, jdbcScala, jdbcClojure, studio).
    settings(
      organization in ThisBuild := "org.eu.acolyte",
      version in ThisBuild := s"1.0.36${versionVariant}",
      javaOptions in ThisBuild ++= Seq(
        "-source", javaVersion, "-target", javaVersion),
      scalaVersion in ThisBuild := "2.11.7",
      crossScalaVersions in ThisBuild := Seq("2.10.4", "2.11.7"),
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
      </developers>) configure { p =>
      if (isJavaAtLeast("1.8")) p.aggregate(jdbcJava8) else p
    }
}

trait Dependencies {
  val specs2Test = "org.specs2" %% "specs2" % "2.4.1" % "test"
}
