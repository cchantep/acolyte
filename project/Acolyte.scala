import sbt._
import Keys._

// Multi-module project build
object Acolyte extends Build {

  lazy val core = Project(id = "core", base = file("core"))
  lazy val scala = Project(id = "scala", base = file("scala")).dependsOn(core)

  val appVersion      = "1.0.0"
  lazy val root = Project(id = "acolyte", base = file(".")).
    aggregate(core, scala).settings(
      scalaVersion := "2.10.0",
      publishTo := Some(Resolver.file("file", 
        new File(Path.userHome.absolutePath+"/.m2/repository"))))
}
