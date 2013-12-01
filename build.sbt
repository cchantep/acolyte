name := "10min-anorm-tutorial"

organization := "acolyte"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3"

resolvers ++= Seq(
  "Applicius Releases" at "https://raw.github.com/applicius/mvn-repo/master/releases/",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "play" %% "anorm" % "2.1.5",
  "org.specs2" %% "specs2" % "2.3.2" % "test",
  "acolyte" %% "acolyte-scala" % "1.0.9" % "test")
