import sbt._
import Keys._
import com.unhandledexpression.sbtclojure.ClojurePlugin.clojure

trait JdbcClojure {
  // Dependencies
  def jdbcDriver: Project

  lazy val clojureSettings = clojure.settings ++ Seq(
    name := "jdbc-clojure",
    javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    autoScalaLibrary := false,
    scalacOptions += "-feature",
    crossPaths := false,
    libraryDependencies ++= Seq(
      "org.clojure" % "clojure" % "1.6.0",
      "org.eu.acolyte" % "jdbc-driver" % (version in ThisBuild).value))

  lazy val jdbcClojure = 
    Project(id = "jdbc-clojure", base = file("jdbc-clojure")).
      settings(clojureSettings: _*).dependsOn(jdbcDriver)
}
