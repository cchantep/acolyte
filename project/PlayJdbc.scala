import sbt._
import Keys._

class PlayJdbc(jdbcScala: Project) {

  import Dependencies._

  val playVersion = settingKey[String]("Playframework version")

  lazy val project =
    Project(id = "play-jdbc", base = file("play-jdbc"))
      .settings(
        name := "play-jdbc",
        playVersion := {
          val scalaVer = scalaBinaryVersion.value

          if (scalaVer == "2.11") "2.5.19"
          else if (scalaVer == "2.12") "2.6.7"
          else if (scalaVer == "2.13") "2.7.9"
          else if (scalaVer == "3") "2.8.16"
          else "2.4.8"
        },
        Compile / unmanagedSourceDirectories += {
          val base = (Compile / sourceDirectory).value

          CrossVersion.partialVersion(playVersion.value) match {
            case Some((maj, min)) => base / s"play-${maj}.${min}"
            case _                => base / "play"
          }
        },
        // make sure plugin is there
        libraryDependencies ++= {
          val anorm = "org.playframework.anorm" %% "anorm" % "2.8.1"

          Seq(
            "org.eu.acolyte" % "jdbc-driver" % (ThisBuild / version).value,
            ("com.typesafe.play" %% "play-jdbc-api" % playVersion.value % Provided)
              .cross(CrossVersion.for3Use2_13 /* TODO */ ),
            (anorm % Test).cross(CrossVersion.for3Use2_13 /* TODO */ ),
            "org.specs2" %% "specs2-core" % specsVer.value % Test
          )
        }
      )
      .dependsOn(jdbcScala)
}
