import sbt._
import Keys._

trait PlayJdbc { deps: Dependencies with Format ⇒
  // Dependencies
  def scalacPlugin: Project
  def jdbcScala: Project

  lazy val playJdbc = 
    Project(id = "play-jdbc", base = file("play-jdbc")).
      settings(formatSettings).settings(
        name := "play-jdbc",
        javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
        scalaVersion := "2.11.8",
        crossScalaVersions := Seq(scalaVersion.value),
        scalacOptions in Compile ++= Seq("-unchecked", "-deprecation"),
        scalacOptions in Compile <++= (scalaVersion in ThisBuild).map { v =>
          if (v startsWith "2.11") Seq("-Ywarn-unused-import")
          else Nil
        },
        scalacOptions in Test <++= (version in ThisBuild).
          zip(scalaVersion in ThisBuild).
          zip(baseDirectory in (scalacPlugin, Compile)).
          zip(name in (scalacPlugin, Compile)) map { d =>
            val (((v, sv), b), n) = d
            val msv =
              if (sv startsWith "2.10") "2.10"
              else if (sv startsWith "2.11") "2.11"
              else sv

            val td = b / "target" / s"scala-$msv"
            val j = td / s"${n}_${msv}-$v.jar"

            Seq("-feature", "-deprecation", s"-Xplugin:${j.getAbsolutePath}")
          },
        compile in Test <<= (compile in Test).
          dependsOn(compile in (scalacPlugin, Test)),
        // make sure plugin is there
        libraryDependencies ++= {
          val playVer = {
            if (scalaVersion.value startsWith "2.11") "2.5.8"
            else "2.4.8"
          }

          Seq(
            "org.eu.acolyte" % "jdbc-driver" % (version in ThisBuild).value,
            "com.typesafe.play" %% "play-jdbc-api" % "2.4.8" % "provided",
            "com.typesafe.play" %% "anorm" % "2.5.2" % Test,
            specs2Test)
        }
      ).dependsOn(scalacPlugin, jdbcScala)

}
