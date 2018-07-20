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

  val reactiveMongoVer = "0.15.0"

  lazy val project =
    Project(id = "reactive-mongo", base = file("reactive-mongo")).
      settings(formatSettings).settings(
        name := "reactive-mongo",
        fork in Test := true,
        javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
        /*scalacOptions ++= {
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
        },*/
        resolvers ++= reactiveResolvers,
        libraryDependencies ++= Seq(
          "org.reactivemongo" %% "reactivemongo" % reactiveMongoVer % "provided",
          "com.jsuereth" %% "scala-arm" % "2.0",
          "org.slf4j" % "slf4j-simple" % "1.7.13" % Test,
          "com.chuusai" %% "shapeless" % "2.3.2",
          specs2Test
        )
      )//.dependsOn(scalacPlugin)

  lazy val playProject =
    Project(id = "play-reactive-mongo", base = file("play-reactive-mongo")).
      settings(formatSettings).settings(
        name := "play-reactive-mongo",
        crossScalaVersions ~= { _.filterNot(_ startsWith "2.10") },
        javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
        scalacOptions ++= {
          val v = version.value
          val sv = scalaVersion.value
          val b = (baseDirectory in (scalacPlugin, Compile)).value
          val n = (name in (scalacPlugin, Compile)).value

          val msv = {
            if (sv startsWith "2.12") "2.12"
            else if (sv startsWith "2.11") "2.11"
            else "2.10"
          }

          val td = b / "target" / s"scala-$msv"
          val j = td / s"${n}_${msv}-$v.jar"

          Seq("-feature", "-deprecation", s"-Xplugin:${j.getAbsolutePath}")
        },
        resolvers ++= reactiveResolvers,
        libraryDependencies ++= {
          val (playVer, playVar) = if (scalaVersion.value startsWith "2.12") {
            "2.6.3" -> "play26"
          } else {
            "2.5.13" -> "play25"
          }

          Seq(
            "com.typesafe.play" %% "play" % playVer % Provided,
            "org.reactivemongo" %% "play2-reactivemongo" % s"${reactiveMongoVer}-${playVar}" % Provided,
            specs2Test
          )
        }
      ).dependsOn(scalacPlugin, self.project)

}
