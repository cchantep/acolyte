import sbt._
import Keys._

object Compiler extends AutoPlugin {
  override def projectSettings = Seq(
    javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
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
    scalacOptions in Compile ++= {
      if (scalaBinaryVersion.value != "2.11") Nil
      else Seq(
        "-Yconst-opt",
        "-Yclosure-elim",
        "-Ydead-code",
        "-Yopt:_"
      )
    },
    scalacOptions in Compile ++= {
      if (scalaBinaryVersion.value == "2.10") Nil
      else Seq(
        "-Ywarn-infer-any",
        "-Ywarn-unused",
        "-Ywarn-unused-import",
        "-Xlint:missing-interpolator"
      )
    },
    scalacOptions in Compile ++= {
      if (scalaBinaryVersion.value != "2.12") Seq("-target:jvm-1.6")
      else Seq("-target:jvm-1.8")
    },
    scalacOptions in (Compile, console) ~= {
      _.filterNot { opt => opt.startsWith("-X") || opt.startsWith("-Y") }
    },
    scalacOptions in (Test, console) ~= {
      _.filterNot { opt => opt.startsWith("-X") || opt.startsWith("-Y") }
    },
    scalacOptions in (Test, console) += "-Yrepl-class-based",
    scalacOptions in (Compile, doc) ++= Seq("-unchecked", "-deprecation",
      /*"-diagrams", */"-implicits", "-skip-packages", "samples"),
    scalacOptions in (Compile, doc) ++= Opts.doc.title(
      s"Acolyte ${name.value}"),
    scalacOptions in (Compile, doc) ++= {
      sbtdynver.DynVerPlugin.autoImport.previousStableVersion.value.map {
        _.takeWhile(_ != '.')
      }.toSeq
    }
  )
}
