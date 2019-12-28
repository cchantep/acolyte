import sbt._
import Keys._

class ReactiveMongo(scalacPlugin: Project) { self =>
  import Dependencies._
  import Format._

  val reactiveResolvers = Seq(
    Resolver.typesafeRepo("snapshots"),
    Resolver.sonatypeRepo("snapshots"))

  lazy val generatedClassDirectory = settingKey[File](
    "Directory where classes get generated")

  val reactiveMongoVer = "0.19.6"

  lazy val project =
    Project(id = "reactive-mongo", base = file("reactive-mongo")).
      settings(formatSettings ++ Set(
        name := "reactive-mongo",
        fork in Test := true,
        resolvers ++= reactiveResolvers,
        crossScalaVersions ~= { _.filterNot(_ startsWith "2.10") },
        scalacOptions in Test ++= ScalacPlugin.
          compilerOptions(scalacPlugin).value,
        libraryDependencies ++= Seq(
          "org.reactivemongo" %% "reactivemongo" % reactiveMongoVer % Provided,
          "com.jsuereth" %% "scala-arm" % "2.1-SNAPSHOT",
          "org.slf4j" % "slf4j-simple" % "1.7.30" % Provided,
          "com.chuusai" %% "shapeless" % "2.3.3",
          "org.specs2" %% "specs2-core" % specsVer.value % Test)
      )).dependsOn(scalacPlugin % Test)

  lazy val playProject =
    Project(id = "play-reactive-mongo", base = file("play-reactive-mongo")).
      settings(formatSettings ++ Set(
        name := "play-reactive-mongo",
        crossScalaVersions ~= { _.filterNot(_ startsWith "2.10") },
        sourceDirectory := {
          if (scalaBinaryVersion.value == "2.10") {
            new java.io.File("/no/sources")
          } else sourceDirectory.value
        },
        scalacOptions in Test ++= ScalacPlugin.
          compilerOptions(scalacPlugin).value,
        resolvers ++= reactiveResolvers,
        publish := (Def.taskDyn {
          val p = publish.value
          val ver = scalaBinaryVersion.value

          Def.task[Unit] {
            if (ver == "2.10") ({})
            else p
          }
        }).value,
        publishTo := (Def.taskDyn {
          val p = publishTo.value
          val ver = scalaBinaryVersion.value

          Def.task {
            if (ver == "2.10") None
            else p
          }
        }).value,
        libraryDependencies ++= {
          val sv = scalaBinaryVersion.value

          if (sv == "2.10") {
            Seq.empty[ModuleID]
          } else {
            val (playVer, playVar) = {
              if (sv == "2.12") "2.6.3" -> "play26"
              else if (sv == "2.13") "2.7.3" -> "play27"
              else "2.5.13" -> "play25"
            }

            val playRmVer = reactiveMongoVer.span(_ != '-') match {
              case (v, m) => s"${v}-${playVar}${m}"
            }

            val iteratees = {
              if (sv != "2.13") {
                Seq(
                  "com.typesafe.play" %% "play-iteratees" % "2.6.1" % Provided)
              } else {
                Seq.empty
              }
            }

            Seq(
              "com.typesafe.play" %% "play" % playVer % Provided,
              "org.reactivemongo" %% "play2-reactivemongo" % playRmVer % Provided,
              "org.specs2" %% "specs2-core" % specsVer.value % Test
            ) ++ iteratees
          }
        }
      )).dependsOn(scalacPlugin, self.project)

}
