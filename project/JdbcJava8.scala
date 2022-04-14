import sbt._
import Keys._

class JdbcJava8(jdbcDriver: Project) {
  import Dependencies._
  
  lazy val project =
    Project(id = "jdbc-java8", base = file("jdbc-java8")).settings(
      name := "jdbc-java8",
      //javacOptions in := Seq("-source", "1.8", "-target", "1.8"),
      Test / javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
      autoScalaLibrary := false,
      libraryDependencies += (
        "org.specs2" %% "specs2-core" % specsVer.value % Test),
      crossPaths := false,
      scalacOptions += "-feature").
      dependsOn(jdbcDriver)
  
}
