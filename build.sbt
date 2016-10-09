name := "10min-anorm-tutorial"

organization := "org.eu.acolyte"

version := "1.0.39"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "anorm" % "2.5.2",
  "org.specs2" %% "specs2-core" % "3.8.3" % "test",
  "org.eu.acolyte" %% "jdbc-scala" % s"${version.value}-j7p" % "test")
