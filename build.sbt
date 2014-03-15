name := "play-demo"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache
)

play.Project.playScalaSettings

scalacOptions += "-feature"
