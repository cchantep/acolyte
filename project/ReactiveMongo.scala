import sbt._
import Keys._

class ReactiveMongo(scalacPlugin: Project) { self =>
  import Dependencies._
  import Format._

  val reactiveResolvers = Seq(
    "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype Snapshots".at(
      "https://oss.sonatype.org/content/repositories/snapshots/"))

  lazy val generatedClassDirectory = settingKey[File](
    "Directory where classes get generated")

  val reactiveMongoVer = "0.19.1"

  lazy val project =
    Project(id = "reactive-mongo", base = file("reactive-mongo")).
      settings(formatSettings ++ Set(
        name := "reactive-mongo",
        fork in Test := true,
        resolvers ++= reactiveResolvers,
        libraryDependencies ++= Seq(
          "org.reactivemongo" %% "reactivemongo" % reactiveMongoVer % Provided,
          "com.jsuereth" %% "scala-arm" % "2.1-SNAPSHOT",
          "org.slf4j" % "slf4j-simple" % "1.7.29" % Provided,
          "com.chuusai" %% "shapeless" % "2.3.3",
          "org.specs2" %% "specs2-core" % specsVer.value % Test)
      ))//.dependsOn(scalacPlugin)

  lazy val playProject =
    Project(id = "play-reactive-mongo", base = file("play-reactive-mongo")).
      settings(formatSettings ++ Set(
        name := "play-reactive-mongo",
        crossScalaVersions ~= { _.filterNot(_ startsWith "2.10") },
        sourceDirectory := {
          if (scalaVersion.value startsWith "2.10.") {
            new java.io.File("/no/sources")
          } else sourceDirectory.value
        },
        scalacOptions ++= {
          val v = version.value
          val sv = scalaVersion.value
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
        resolvers ++= reactiveResolvers,
        publish := (Def.taskDyn {
          val p = publish.value
          val ver = scalaVersion.value

          Def.task[Unit] {
            if (ver startsWith "2.10.") ({})
            else p
          }
        }).value,
        publishTo := (Def.taskDyn {
          val p = publishTo.value
          val ver = scalaVersion.value

          Def.task {
            if (ver startsWith "2.10.") None
            else p
          }
        }).value,
        libraryDependencies ++= {
          val sv = scalaVersion.value

          if (sv startsWith "2.10.") {
            Seq.empty[ModuleID]
          } else {
            val (playVer, playVar) = {
              if (sv startsWith "2.12.") "2.6.3" -> "play26"
              else if (sv startsWith "2.13.") "2.7.3" -> "play27"
              else "2.5.13" -> "play25"
            }

            val playRmVer = reactiveMongoVer.span(_ != '-') match {
              case (v, m) => s"${v}-${playVar}${m}"
            }

            Seq(
              "com.typesafe.play" %% "play" % playVer % Provided,
              "org.reactivemongo" %% "play2-reactivemongo" % playRmVer % Provided,
              "org.specs2" %% "specs2-core" % specsVer.value % Test
            )
          }
        }
      )).dependsOn(scalacPlugin, self.project)

}
