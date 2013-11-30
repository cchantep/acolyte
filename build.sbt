name := "10min-anorm-tutorial"

organization := "acolyte"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3"

resolvers += "Applicius Releases" at "https://raw.github.com/applicius/mvn-repo/master/releases/"

libraryDependencies ++= Seq(
  "play" %% "anorm" % "2.1.2",
  "org.specs2" %% "specs2" % "2.3.2" % "test"
  "acolyte" %% "acolyte" % "1.0.9" % test")
