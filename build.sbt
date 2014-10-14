name := "reactivemongo-tutorial"

scalaVersion := "2.11.3"

organization := "org.eu.applidok"

version := "1.0.29"

autoCompilerPlugins := true

addCompilerPlugin("org.eu.acolyte" %% "scalac-plugin" % "1.0.29")

mainClass in (Compile, run) := Some("applidok.SbtRunner")

//scalacOptions += "-P:acolyte:debug"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
  "org.specs2" %% "specs2" % "2.4.6-scalaz-7.0.6" % "test",
  "org.eu.acolyte" %% "reactive-mongo" % "1.0.29" % "test"
)
