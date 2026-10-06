organization := "org.eu.acolyte"

name := "play-demo"

version := "1.4"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  guice,
  "org.eu.acolyte" %% "jdbc-scala" % "1.2.10"
)
