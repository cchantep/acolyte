import sbt._
import Keys._

trait JdbcJava8 { deps: Dependencies ⇒
  // Dependencies
  def jdbcDriver: Project
  
  lazy val jdbcJava8 =
    Project(id = "jdbc-java8", base = file("jdbc-java8")).settings(
      name := "jdbc-java8",
      javacOptions := Seq("-source", "1.8", "-target", "1.8"),
      javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
      scalaVersion := "2.11.6",
      autoScalaLibrary := false,
      libraryDependencies += specs2Test,
      crossPaths := false,
      scalacOptions += "-feature").
      dependsOn(jdbcDriver)
  
}
