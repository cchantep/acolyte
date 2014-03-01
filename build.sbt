name := "10min-anorm-tutorial"

organization := "org.eu.acolyte"

version := "1.0.14"

scalaVersion := "2.10.3"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "play" %% "anorm" % "2.1.5",
  "org.specs2" %% "specs2" % "2.3.8" % "test",
  "org.eu.acolyte" %% "jdbc-scala" % "1.0.14" % "test")
