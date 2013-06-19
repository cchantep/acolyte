import sbt._
import Keys._

// Multi-module project build
object Acolyte extends Build {
  lazy val core = Project(id = "core", base = file("core"))
  lazy val scala = Project(id = "scala", base = file("scala"))
}
