organization := "org.eu.acolyte"

name := "acolyte-site"

val ver = "1.0.45"
val PlayVer = "2.5.13"

version := ver

lazy val `acolyte-site` = (project in file(".")).settings(
  scalaVersion := "2.11.11",
  scalacOptions in ThisBuild ++= Seq("-Ywarn-unused-import", "-unchecked"),
  libraryDependencies ++= Seq(
    "org.reactivemongo" %% "play2-reactivemongo" % "0.12.5-play25",
    "org.eu.acolyte" %% "play-jdbc" % ver,
    "org.eu.acolyte" %% "play-reactive-mongo" % ver,
    "org.specs2" %% "specs2-core" % "3.9.4",
    "com.typesafe.play" %% "play-test" % PlayVer,
    "com.typesafe.play" %% "play-jdbc" % PlayVer
  ),
  resolvers in ThisBuild += "Sonatype Staging" at "https://oss.sonatype.org/content/repositories/staging/",
  autoCompilerPlugins := true,
  addCompilerPlugin("org.eu.acolyte" %% "scalac-plugin" % ver)
)
