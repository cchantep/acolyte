name := "acolyte-rowparsers"

organization := "acolyte"

version := "1.0.8"

scalaVersion := "2.10.3"

javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

scalacOptions += "-feature"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

libraryDependencies ++= Seq(
  "acolyte" % "acolyte-core" % "1.0.8",
  "org.specs2" %% "specs2" % "1.14" % "test")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
