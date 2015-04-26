organization := "org.eu.acolyte"

name := "reactivemongo-demo"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.jsuereth" %% "scala-arm" % "1.4",
  "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
  "org.eu.acolyte" %% "reactive-mongo" % "1.0.33" changing()
)
