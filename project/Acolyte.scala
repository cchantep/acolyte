import sbt._
import Keys._

// Multi-module project build
object Acolyte extends Build 
    with JdbcDriver with ScalacPlugin with JdbcScala with Studio {

  lazy val root = Project(id = "acolyte", base = file(".")).
    aggregate(jdbcDriver, scalacPlugin, jdbcScala, studio).
    settings(
      version := "1.0.14",
      scalaVersion := "2.10.3",
      publishTo := Some(Resolver.file("file", 
        new File(Path.userHome.absolutePath+"/.m2/repository"))))
}
