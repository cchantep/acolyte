name := "reactivemongo-tutorial"

scalaVersion := "2.12.6"

organization := "org.eu.acolyte"

val ver = "1.0.48"

version := ver

autoCompilerPlugins := true

addCompilerPlugin("org.eu.acolyte" %% "scalac-plugin" % ver)

//scalacOptions += "-P:acolyte:debug"

resolvers ++= Seq(
  "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype Staging" at "https://oss.sonatype.org/content/repositories/staging/"
)

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % "0.15.0") ++ Seq(
  "org.specs2" %% "specs2-core" % "4.3.2",
    "com.typesafe.akka" %% "akka-slf4j" % "2.5.7",
    "org.eu.acolyte" %% "reactive-mongo" % version.value,
    "org.slf4j" % "slf4j-simple" % "1.7.13").map(_ % Test)
