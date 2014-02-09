import sbt._
import Keys._

trait ScalaMacros {
  lazy val scalaMacros = Project(
    id = "scala-macros", base = file("scala-macros")).settings(
    name := "scala-macros",
    organization := "org.eu.acolyte",
    version := "1.0.13",
    scalaVersion := "2.10.3",
    javaOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    scalacOptions ++= Seq("-feature", "-deprecation"),
    resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % "2.10.3",
      "org.specs2" %% "specs2" % "2.3.2" % "test"),
    publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository"))),
    pomExtra := (
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
