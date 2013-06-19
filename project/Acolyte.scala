import sbt._
import Keys._

// Multi-module project build
object Acolyte extends Build {
  lazy val core = Project(id = "core", base = file("core"))
}
