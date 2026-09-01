import sbt._
import Keys._

final class ReactiveMongo { self =>
  import Dependencies._

  lazy val generatedClassDirectory =
    settingKey[File]("Directory where classes get generated")

  val reactiveMongoVer = "1.1.0-RC13"

  lazy val reactiveMongoPekkoVer = reactiveMongoVer.span(_ != '-') match {
    case (v, mod) =>
      (if (mod != "") mod.drop(1) else mod).span(_ != '-') match {
        case ("", _) =>
          s"${v}-pekko"

        case (a, "") if (a.startsWith("RC")) =>
          s"${v}-pekko.${a}"

        case (a, b) =>
          s"${v}-${a}-pekko${b}"
      }
  }

  private def coreProject(suffix: String, depVer: String) =
    Project(
      id = s"reactive-mongo-core-$suffix",
      base = file("reactive-mongo-core")
    ).settings(
      name := s"reactive-mongo-core-$suffix",
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
        "org.reactivemongo" %% "reactivemongo" % depVer % Provided,
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

  lazy val akkaCoreProject = coreProject("akka", reactiveMongoVer)

  lazy val pekkoCoreProject = coreProject("pekko", reactiveMongoPekkoVer)

  private def project(suffix: String, depVersion: String) =
    Project(
      id = s"reactive-mongo-$suffix",
      base = file(s"reactive-mongo-$suffix")
    ).settings(
      name := s"reactive-mongo-$suffix",
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
        "org.reactivemongo" %% "reactivemongo" % depVersion % Provided,
        "org.slf4j" % "slf4j-simple" % "2.0.18" % Provided,
        "org.specs2" %% "specs2-core" % specsVer.value % Test
      )
    )

  lazy val akkaProject = project("akka", reactiveMongoVer).dependsOn(
    sbt.projectToLocalProject(self.akkaCoreProject) % "provided;test->test"
  )

  lazy val pekkoProject = project("pekko", reactiveMongoPekkoVer).dependsOn(
    sbt.projectToLocalProject(self.pekkoCoreProject) % "provided;test->test"
  )

  lazy val playProject =
    Project(id = "play-reactive-mongo", base = file("play-reactive-mongo"))
      .settings(
        name := "play-reactive-mongo",
        libraryDependencies ++= {
          val sv = (Compile / scalaBinaryVersion).value

          val (playVer, playVar) = {
            if (sv == "2.12") "2.6.3" -> "play26"
            else if (sv == "2.13") "2.7.9" -> "play27"
            else if (sv == "3") "3.0.5" -> "play30"
            else "2.5.19" -> "play25"
          }

          val playRmVer = reactiveMongoVer.span(_ != '-') match {
            case (v, mod) =>
              (if (mod != "") mod.drop(1) else mod).span(_ != '-') match {
                case ("", _) =>
                  s"${v}-${playVar}"

                case (a, "") if (a.startsWith("RC")) =>
                  s"${v}-${playVar}.${a}"

                case (a, b) =>
                  s"${v}-${a}-${playVar}${b}"
              }
          }

          val iteratees = {
            if (sv != "2.13" && sv != "3") {
              Seq("com.typesafe.play" %% "play-iteratees" % "2.6.1" % Provided)
            } else {
              Seq.empty
            }
          }

          val playOrg = {
            if (playVer.startsWith("2.")) {
              "com.typesafe.play"
            } else {
              "org.playframework"
            }
          }

          (Seq("reactivemongo-play-json-compat", "play2-reactivemongo").map {
            "org.reactivemongo" %% _ % playRmVer % Provided
          }) ++ Seq(
            playOrg %% "play" % playVer % Provided,
            "org.specs2" %% "specs2-core" % specsVer.value % Test
          ) ++ iteratees
        }
      )
      .dependsOn(sbt.projectToLocalProject(self.akkaProject))

}
