import sbt._
import Keys._

object ScalacPlugin {
  import Dependencies._
  import Format._

  lazy val project = 
    Project(id = "scalac-plugin", base = file("scalac-plugin")).
      settings(formatSettings).settings(
        name := "scalac-plugin",
        javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
        libraryDependencies ++= Seq(
          "org.scala-lang" % "scala-compiler" % scalaVersion.value, specs2Test),
        compile in Test := (compile in Test).dependsOn(
          packageBin in Compile/* make sure plugin.jar is available */).value,
        scalacOptions in Compile ++= Seq("-feature", "-deprecation"),
        sourceGenerators in Compile += Def.task[Seq[File]] {
          val ver = scalaVersion.value
          val dir = (sourceManaged in Compile).value

          generateUtility(ver, dir)
        },
        scalacOptions in Test ++= {
          val v = version.value
          val sv = scalaVersion.value
          val b = (baseDirectory in Compile).value
          val n = (name in Compile).value
          val msv =
            if (sv startsWith "2.10") "2.10"
            else if (sv startsWith "2.11") "2.11"
            else if (sv startsWith "2.12") "2.12"
            else sv

          val td = b / "target" / s"scala-$msv"
          val j = td / s"${n}_$msv-$v.jar"

          Seq("-feature", "-deprecation", "-P:acolyte:debug",
            s"-Xplugin:${j.getAbsolutePath}")
        })

  private def generateUtility(ver: String, managed: File): Seq[File] = {
    val f = managed / "acolyte" / "CompilerUtility.scala"
    val withSource = 
      if (ver startsWith "2.11") "withSource(f).withShift(shift)"
      else "withSource(f, shift)"

    IO.writer[Seq[File]](f, "", IO.defaultCharset, false) { w ⇒
      w append {
        s"""package acolyte

import scala.tools.nsc.Global
import scala.reflect.internal.util.BatchSourceFile

object CompilerUtility {
  @inline def withSource(global: Global)(pos: global.Position, f: BatchSourceFile, shift: Int): global.Position = pos.$withSource
}"""
      }

      Seq(f)
    }
  }
}
