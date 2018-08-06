name := "10min-anorm-tutorial"

organization := "org.eu.acolyte"

version := "1.0.49"

scalaVersion := "2.12.6"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases")

libraryDependencies ++= Seq(
  "org.playframework.anorm" %% "anorm" % "2.6.2",
  "org.specs2" %% "specs2-core" % "4.3.2" % Test,
  "org.eu.acolyte" %% "jdbc-scala" % version.value % Test)
