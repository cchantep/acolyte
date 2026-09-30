name := "10min-anorm-tutorial"

organization := "org.eu.acolyte"

version := "1.2.10"

scalaVersion := "2.13.18"

resolvers ++= Seq(
  "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "https://repo.typesafe.com/typesafe/snapshots/")

libraryDependencies ++= Seq(
  "org.playframework.anorm" %% "anorm" % "2.8.1",
  "org.specs2" %% "specs2-core" % "4.15.0" % Test,
  "org.eu.acolyte" %% "jdbc-scala" % version.value % Test)
