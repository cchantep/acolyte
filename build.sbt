name := "10min-anorm-tutorial"

organization := "org.eu.acolyte"

version := "1.0.21"

scalaVersion := "2.10.4"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "anorm" % "2.3.0",
  "org.specs2" %% "specs2" % "2.3.11" % "test",
  "org.eu.acolyte" %% "jdbc-scala" % "1.0.21" % "test")
