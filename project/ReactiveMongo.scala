import sbt._
import Keys._

final class ReactiveMongo(scalacPlugin: Project) { self =>
  import Dependencies._
  import Format._

  val reactiveResolvers = Seq(
    Resolver.typesafeRepo("snapshots"),
    Resolver.sonatypeRepo("snapshots"))

  lazy val generatedClassDirectory = settingKey[File](
    "Directory where classes get generated")

  val reactiveMongoVer = "1.0.8"

  lazy val project =
    Project(id = "reactive-mongo", base = file("reactive-mongo")).
      settings(formatSettings ++ Set(
        name := "reactive-mongo",
        fork in Test := true,
        resolvers ++= reactiveResolvers,
        compile in Test := (compile in Test).dependsOn(
          scalacPlugin / packageBin in Compile).value,
        scalacOptions in Test ++= ScalacPlugin.
          compilerOptions(scalacPlugin).value,
        libraryDependencies ++= Seq(
          "org.reactivemongo" %% "reactivemongo" % reactiveMongoVer % Provided,
          "com.jsuereth" %% "scala-arm" % "2.1-SNAPSHOT",
          "org.slf4j" % "slf4j-simple" % "1.7.32" % Provided,
          "com.chuusai" %% "shapeless" % "2.3.7",
          "org.specs2" %% "specs2-core" % specsVer.value % Test)
      ))

  lazy val playProject =
    Project(id = "play-reactive-mongo", base = file("play-reactive-mongo")).
      settings(formatSettings ++ Set(
        name := "play-reactive-mongo",
        compile in Test := (compile in Test).dependsOn(
          scalacPlugin / packageBin in Compile).value,
        scalacOptions in Test ++= ScalacPlugin.
          compilerOptions(scalacPlugin).value,
        resolvers ++= reactiveResolvers,
        libraryDependencies ++= {
          val sv = scalaBinaryVersion.value

          val (playVer, playVar) = {
            if (sv == "2.12") "2.6.3" -> "play26"
            else if (sv == "2.13") "2.7.3" -> "play27"
            else "2.5.19" -> "play25"
          }

          val playRmVer = reactiveMongoVer.span(_ != '-') match {
            case (v, mod) =>
              (if (mod != "") mod.drop(1) else mod).span(_ != '-') match {
                case ("", _) =>
                  s"${v}-${playVar}"

                case (a, "") if (a startsWith "rc.") =>
                  s"${v}-${playVar}-${a}"

                case (a, b) =>
                  s"${v}-${a}-${playVar}${b}"
              }
          }

          val iteratees = {
            if (sv != "2.13") {
              Seq(
                "com.typesafe.play" %% "play-iteratees" % "2.6.1" % Provided)
            } else {
              Seq.empty
            }
          }

          (Seq("reactivemongo-play-json-compat", "play2-reactivemongo").map {
            "org.reactivemongo" %% _ % playRmVer % Provided
          }) ++ Seq(
            "com.typesafe.play" %% "play" % playVer % Provided,
            "org.specs2" %% "specs2-core" % specsVer.value % Test
          ) ++ iteratees
        }
      )).dependsOn(self.project)

}
