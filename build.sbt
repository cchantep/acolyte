organization := "org.eu.acolyte"

name := "play-demo"

version := "1.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "com.jsuereth" %% "scala-arm" % "1.4",
  "org.eu.acolyte" %% "jdbc-scala" % "1.0.22" changing()
)

// scalacOptions ++= Seq("-feature", "-P:acolyte:debug")

// autoCompilerPlugins := true

// addCompilerPlugin("org.eu.acolyte" %% "scalac-plugin" % "1.0.22")
