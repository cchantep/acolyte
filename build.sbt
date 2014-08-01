name := "10min-anorm-tutorial"

organization := "org.eu.acolyte"

version := "1.0.22"

scalaVersion := "2.11.2"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "anorm" % "2.3.3",
  "org.specs2" %% "specs2" % "2.3.12" % "test",
  "org.eu.acolyte" %% "jdbc-scala" % "1.0.22" % "test")
