name := "10min-anorm-tutorial"

organization := "org.eu.acolyte"

version := "1.0.45"

scalaVersion := "2.12.2"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "anorm" % "2.5.3",
  "org.specs2" %% "specs2-core" % "3.9.4" % Test,
  "org.eu.acolyte" %% "jdbc-scala" % version.value % Test)
