organization := "org.eu.acolyte"

name := "acolyte-site"

val ver = "1.0.52"
val PlayVer = "2.6.7"

version := ver

lazy val `acolyte-site` = (project in file(".")).settings(
  scalaVersion := "2.12.8",
  scalacOptions in ThisBuild ++= Seq("-Ywarn-unused-import", "-unchecked"),
  libraryDependencies ++= Seq(
    "org.reactivemongo" %% "play2-reactivemongo" % "0.18.1-play26",
    "org.eu.acolyte" %% "play-jdbc" % ver,
    "org.eu.acolyte" %% "play-reactive-mongo" % ver,
    "org.specs2" %% "specs2-core" % "4.6.0",
    "com.typesafe.play" %% "play-test" % PlayVer,
    "com.typesafe.play" %% "play-jdbc" % PlayVer
  ),
  resolvers in ThisBuild ++= Seq(
    "Tatami Snapshots" at "https://raw.github.com/cchantep/tatami/master/snapshots"),
  autoCompilerPlugins := true,
  addCompilerPlugin("org.eu.acolyte" %% "scalac-plugin" % ver)
)
