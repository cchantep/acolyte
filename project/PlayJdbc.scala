import sbt._
import Keys._

class PlayJdbc(
  scalacPlugin: Project,
  jdbcScala: Project) {

  import Dependencies._
  import Format._

  lazy val project = 
    Project(id = "play-jdbc", base = file("play-jdbc")).
      settings(formatSettings).settings(
        name := "play-jdbc",
        javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
        scalacOptions in Compile ++= Seq("-unchecked", "-deprecation"),
        scalacOptions in Compile ++= {
          val v = (scalaVersion in ThisBuild).value

          if (v startsWith "2.11") Seq("-Ywarn-unused-import")
          else Nil
        },
        scalacOptions in Test ++= {
          val v = (version in ThisBuild).value
          val sv = (scalaVersion in ThisBuild).value
          val b = (baseDirectory in (scalacPlugin, Compile)).value
          val n = (name in (scalacPlugin, Compile)).value

          val msv = {
            if (sv startsWith "2.10") "2.10"
            else if (sv startsWith "2.11") "2.11"
            else if (sv startsWith "2.12") "2.12"
            else sv
          }

          val td = b / "target" / s"scala-$msv"
          val j = td / s"${n}_${msv}-$v.jar"

          Seq("-feature", "-deprecation", s"-Xplugin:${j.getAbsolutePath}")
        },
        compile in Test := (compile in Test).
          dependsOn(compile in (scalacPlugin, Test)).value,
        // make sure plugin is there
        libraryDependencies ++= {
          val (playVer, anormVer) = {
            if (scalaVersion.value startsWith "2.11") "2.5.8" -> "2.5.2"
            if (scalaVersion.value startsWith "2.12") "2.6.0" -> "2.5.3"
            else "2.4.8" -> "2.5.0"
          }

          Seq(
            "org.eu.acolyte" % "jdbc-driver" % (version in ThisBuild).value,
            "com.typesafe.play" %% "play-jdbc-api" % playVer % "provided",
            "com.typesafe.play" %% "anorm" % anormVer % Test,
            "org.specs2" %% "specs2-core" % specsVer.value % Test)
        }
      ).dependsOn(scalacPlugin, jdbcScala)

}
