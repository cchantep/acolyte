import sbt._
import Keys._

// Multi-module project build
object Acolyte extends Build with Dependencies 
    with ScalacPlugin with ReactiveMongo 
    with JdbcDriver with JdbcScala with Studio {

  lazy val root = Project(id = "acolyte", base = file(".")).
    aggregate(scalacPlugin, reactiveMongo, jdbcDriver, jdbcScala, studio).
    settings(
      organization in ThisBuild := "org.eu.acolyte",
      version in ThisBuild := "1.0.28",
      javaOptions in ThisBuild ++= Seq("-source", "1.6", "-target", "1.6"),
      scalaVersion in ThisBuild := "2.10.4",
      crossScalaVersions in ThisBuild := Seq("2.10.4", "2.11.2"),
      publishTo in ThisBuild := Some(Resolver.file("file", 
        new File(Path.userHome.absolutePath+"/.m2/repository"))),
      pomExtra in ThisBuild := (
      <url>https://github.com/cchantep/acolyte/</url>
      <licenses>
        <license>
          <name>GNU Lesser General Public License, Version 2.1</name>
          <url>
            https://raw.github.com/cchantep/acolyte/master/LICENSE.txt
          </url>
          <distribution>repo</distribution>
        </license>
      </licenses>
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
      </developers>))
}

trait Dependencies {
  val specs2Test = "org.specs2" %% "specs2" % "2.4.1" % "test"
}
