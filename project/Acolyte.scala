import sbt._
import Keys._

// Multi-module project build
object Acolyte extends Build 
    with Core with ScalaCompiler with Scala with Studio {

  lazy val root = Project(id = "acolyte", base = file(".")).
    aggregate(core, scalaCompiler, scala, studio).
    settings(
      version := "1.0.14",
      scalaVersion := "2.10.3",
      publishTo := Some(Resolver.file("file", 
        new File(Path.userHome.absolutePath+"/.m2/repository"))))
}
