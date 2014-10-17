name := "reactivemongo-tutorial"

scalaVersion := "2.11.3"

organization := "org.eu.applidok"

version := "1.0.29"

autoCompilerPlugins := true

addCompilerPlugin("org.eu.acolyte" %% "scalac-plugin" % "1.0.29")

mainClass in (Compile, run) := Some("applidok.SbtRunner")

//scalacOptions += "-P:acolyte:debug"

resolvers ++= Seq(
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/")

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
  "org.specs2" %% "specs2" % "2.4.6" % "test",
  "org.eu.acolyte" %% "reactive-mongo" % "1.0.29" % "test"
)
