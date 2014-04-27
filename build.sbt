name := "play-demo"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  // jdbc,
  "org.eu.acolyte" %% "jdbc-scala" % "1.0.16" 
)

play.Project.playScalaSettings

scalacOptions += "-feature"
