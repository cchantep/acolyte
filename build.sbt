name := "reactivemongo-tutorial"

scalaVersion := "2.11.8"

organization := "org.eu.acolyte"

version := "1.0.41"

autoCompilerPlugins := true

addCompilerPlugin("org.eu.acolyte" %% "scalac-plugin" % "1.0.41-j7p")

mainClass in (Compile, run) := Some("applidok.SbtRunner")

//scalacOptions += "-P:acolyte:debug"

resolvers ++= Seq(
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype Staging" at "https://oss.sonatype.org/content/repositories/staging/"
)

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.12-RC6") ++ Seq(
  "org.specs2" %% "specs2-core" % "3.8.3",
    "org.eu.acolyte" %% "reactive-mongo" % s"${version.value}-j7p",
    "org.slf4j" % "slf4j-simple" % "1.7.13").map(_ % Test)
