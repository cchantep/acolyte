import sbt._
import Keys._

final class ReactiveMongo { self =>
  import Dependencies._

  lazy val generatedClassDirectory =
    settingKey[File]("Directory where classes get generated")

  val reactiveMongoVer = "1.1.0-pekko.RC21"

  lazy val project =
    Project(id = "reactive-mongo", base = file("reactive-mongo")).settings(
      name := "reactive-mongo",
      crossScalaVersions := Seq("2.12.20", "2.13.18", "3.4.3"),
      Test / fork := true,
      Compile / unmanagedSourceDirectories ++= {
        val base = (Compile / sourceDirectory).value

        CrossVersion.partialVersion(scalaVersion.value) match {
          case Some((2, n)) if n < 13 =>
            Seq(base / "scala-2.13-")

          case _ =>
            Seq(base / "scala-2.13+")

        }
      },
      libraryDependencies ++= Seq(
        "org.reactivemongo" %% "reactivemongo" % reactiveMongoVer % Provided,
        "org.slf4j" % "slf4j-simple" % "2.0.18" % Provided,
        "org.specs2" %% "specs2-core" % specsVer.value % Test
      ),
      libraryDependencies += {
        if (scalaBinaryVersion.value == "3") {
          "org.typelevel" %% "shapeless3-test" % "3.6.0"
        } else {
          "com.chuusai" %% "shapeless" % "2.3.11"
        }
      }
    )

  lazy val playProject =
    Project(id = "play-reactive-mongo", base = file("play-reactive-mongo"))
      .settings(
        name := "play-reactive-mongo",
        crossScalaVersions := Seq("2.13.18", "3.4.3"),
        libraryDependencies ++= {
          val playVer = "3.0.5"
          val playVar = "play30"
          val rc = reactiveMongoVer.split('.').last
          val playRmVer = s"1.1.0-$playVar.$rc"

          Seq(
            "org.reactivemongo" %% "reactivemongo-play-json-compat" % playRmVer % Provided,
            "org.reactivemongo" %% "play2-reactivemongo" % playRmVer % Provided,
            "org.playframework" %% "play" % playVer % Provided,
            "org.specs2" %% "specs2-core" % specsVer.value % Test
          )
        }
      )
      .dependsOn(self.project)

}
