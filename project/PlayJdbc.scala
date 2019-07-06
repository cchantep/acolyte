import sbt._
import Keys._

class PlayJdbc(
  scalacPlugin: Project,
  jdbcScala: Project) {

  import Dependencies._
  import Format._

  val playVersion = settingKey[String]("Playframework version")

  lazy val project = 
    Project(id = "play-jdbc", base = file("play-jdbc")).
      settings(formatSettings ++ Seq(
        name := "play-jdbc",
        scalacOptions in Test ++= {
          val v = (version in ThisBuild).value
          val sv = (scalaVersion in Test).value
          val b = (baseDirectory in (scalacPlugin, Compile)).value
          val n = (name in (scalacPlugin, Compile)).value

          val msv = CrossVersion.partialVersion(sv) match {
            case Some((maj, min)) => s"${maj}.${min}"
            case _ => sv
          }

          val td = b / "target" / s"scala-$msv"
          val j = td / s"${n}_${msv}-$v.jar"

          Seq("-feature", "-deprecation", s"-Xplugin:${j.getAbsolutePath}")
        },
        playVersion := {
          val scalaVer = scalaVersion.value

          if (scalaVer startsWith "2.11.") "2.5.8"
          else if (scalaVer startsWith "2.12.") "2.6.7"
          else if (scalaVer startsWith "2.13.") "2.7.3"
          else "2.4.8"
        },
        unmanagedSourceDirectories in Compile += {
          val base = (sourceDirectory in Compile).value

          CrossVersion.partialVersion(playVersion.value) match {
            case Some((maj, min)) => base / s"play-${maj}.${min}"
            case _                => base / "play"
          }
        },
        compile in Test := (compile in Test).
          dependsOn(compile in (scalacPlugin, Test)).value,
        // make sure plugin is there
        libraryDependencies ++= {
          val anorm = {
            if (scalaVersion.value startsWith "2.10.") {
              "com.typesafe.play" %% "anorm" % "2.5.0"
            } else {
              "org.playframework.anorm" %% "anorm" % "2.7.3"
            }
          }

          Seq(
            "org.eu.acolyte" % "jdbc-driver" % (version in ThisBuild).value,
            "com.typesafe.play" %% "play-jdbc-api" % playVersion.value % "provided",
            anorm % Test,
            "org.specs2" %% "specs2-core" % specsVer.value % Test)
        }
      )).dependsOn(scalacPlugin, jdbcScala)

}
