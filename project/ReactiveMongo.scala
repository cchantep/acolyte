import sbt._
import Keys._

final class ReactiveMongo { self =>
  import Dependencies._

  lazy val generatedClassDirectory =
    settingKey[File]("Directory where classes get generated")

  val reactiveMongoVer = "1.1.0-RC10"

  lazy val project =
    Project(id = "reactive-mongo", base = file("reactive-mongo")).settings(
      name := "reactive-mongo",
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
        "org.slf4j" % "slf4j-simple" % "2.0.16" % Provided,
        "org.specs2" %% "specs2-core" % specsVer.value % Test
      ),
      libraryDependencies += {
        if (scalaBinaryVersion.value == "3") {
          "org.typelevel" %% "shapeless3-test" % "3.4.3"
        } else {
          "com.chuusai" %% "shapeless" % "2.3.10",
        }
      }
    )

  lazy val playProject =
    Project(id = "play-reactive-mongo", base = file("play-reactive-mongo"))
      .settings(
        name := "play-reactive-mongo",
        libraryDependencies ++= {
          val sv = scalaBinaryVersion.value

          val (playVer, playVar) = {
            if (sv == "2.12") "2.6.3" -> "play26"
            else if (sv == "2.13") "2.7.9" -> "play27"
            else if (sv == "3") "2.8.16" -> "play28"
            else "2.5.19" -> "play25"
          }

          val playRmVer = reactiveMongoVer.span(_ != '-') match {
            case (v, mod) =>
              (if (mod != "") mod.drop(1) else mod).span(_ != '-') match {
                case ("", _) =>
                  s"${v}-${playVar}"

                case (a, "") if (a startsWith "RC") =>
                  s"${v}-${playVar}-${a}"

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

          (Seq("reactivemongo-play-json-compat", "play2-reactivemongo").map {
            "org.reactivemongo" %% _ % playRmVer % Provided
          }) ++ Seq(
            ("com.typesafe.play" %% "play" % playVer % Provided)
              .cross(CrossVersion.for3Use2_13 /* TODO */ ),
            "org.specs2" %% "specs2-core" % specsVer.value % Test
          ) ++ iteratees
        }
      )
      .dependsOn(self.project)

}
