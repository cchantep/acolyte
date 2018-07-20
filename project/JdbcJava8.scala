import sbt._
import Keys._

class JdbcJava8(jdbcDriver: Project) {
  import Dependencies._
  
  lazy val project =
    Project(id = "jdbc-java8", base = file("jdbc-java8")).settings(
      name := "jdbc-java8",
      //javacOptions in := Seq("-source", "1.8", "-target", "1.8"),
      javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
      autoScalaLibrary := false,
      libraryDependencies += specs2Test,
      crossPaths := false,
      scalacOptions += "-feature").
      dependsOn(jdbcDriver)
  
}
