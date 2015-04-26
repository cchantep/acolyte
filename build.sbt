name := "10min-anorm-tutorial"

organization := "org.eu.acolyte"

version := "1.0.33"

scalaVersion := "2.11.6"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "anorm" % "2.4-SNAPSHOT",
  "org.specs2" %% "specs2" % "2.4.6" % "test",
  "org.eu.acolyte" %% "jdbc-scala" % "1.0.33" % "test")
