organization := "org.eu.acolyte"

name := "acolyte-site"

val ver = "1.0.43"
val PlayVer = "2.4.9"

version := ver

lazy val `acolyte-site` = (project in file(".")).settings(
  scalaVersion := "2.11.8",
  scalacOptions in ThisBuild ++= Seq("-Ywarn-unused-import", "-unchecked"),
  libraryDependencies ++= Seq(
    "org.reactivemongo" %% "play2-reactivemongo" % "0.12.1",
    "org.eu.acolyte" %% "play-jdbc" % s"$ver-j7p",
    "org.eu.acolyte" %% "play-reactive-mongo" % s"$ver-j7p",
    "org.specs2" %% "specs2-core" % "3.8.6",
    "com.typesafe.play" %% "play-test" % PlayVer,
    "com.typesafe.play" %% "play-jdbc" % PlayVer
  ),
  resolvers in ThisBuild += "Sonatype Staging" at "https://oss.sonatype.org/content/repositories/staging/",
  autoCompilerPlugins := true,
  addCompilerPlugin("org.eu.acolyte" %% "scalac-plugin" % s"$ver-j7p")
)
