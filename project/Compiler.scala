import sbt._
import Keys._

object Compiler extends AutoPlugin {
  override def projectSettings = Seq(
    Test / javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-unchecked",
      "-deprecation",
      "-feature",
      "-Xfatal-warnings",
      "-Xlint",
      "-Ywarn-numeric-widen",
      "-Ywarn-dead-code",
      "-Ywarn-value-discard",
      "-g:vars"
    ),
    Compile / scalacOptions ++= {
      if (scalaBinaryVersion.value != "2.11") Nil
      else Seq(
        "-Yconst-opt",
        "-Yclosure-elim",
        "-Ydead-code",
        "-Yopt:_"
      )
    },
    Compile / scalacOptions ++= {
      if (scalaBinaryVersion.value == "2.10") Nil
      else Seq(
        "-Ywarn-infer-any",
        "-Ywarn-unused",
        "-Ywarn-unused-import",
        "-Xlint:missing-interpolator"
      )
    },
    Compile / scalacOptions ++= {
      if (scalaBinaryVersion.value != "2.12") Seq("-target:jvm-1.6")
      else Seq("-target:jvm-1.8")
    },
    Compile / console / scalacOptions ~= {
      _.filterNot { opt => opt.startsWith("-X") || opt.startsWith("-Y") }
    },
    Test / console / scalacOptions ~= {
      _.filterNot { opt => opt.startsWith("-X") || opt.startsWith("-Y") }
    },
    Test / console / scalacOptions += "-Yrepl-class-based",
    Compile / doc / scalacOptions ++= Seq("-unchecked", "-deprecation",
      /*"-diagrams", */"-implicits", "-skip-packages", "samples"),
    Compile / doc / scalacOptions ++= Opts.doc.title(
      s"Acolyte ${name.value}"),
    Compile / doc / scalacOptions ++= {
      sbtdynver.DynVerPlugin.autoImport.previousStableVersion.value.map {
        _.takeWhile(_ != '.')
      }.toSeq
    }
  )
}
