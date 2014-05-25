organization := "org.eu.acolyte"

name := "play-demo"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "com.jsuereth" %% "scala-arm" % "1.4",
  "org.eu.acolyte" %% "jdbc-scala" % "1.0.19" changing()
)

play.Project.playScalaSettings

// scalacOptions ++= Seq("-feature", "-P:acolyte:debug")

// autoCompilerPlugins := true

// addCompilerPlugin("org.eu.acolyte" %% "scalac-plugin" % "1.0.19")
