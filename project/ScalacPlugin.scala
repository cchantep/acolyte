import sbt._
import Keys._

object ScalacPlugin {
  import Dependencies._
  import Format._

  lazy val project = 
    Project(id = "scalac-plugin", base = file("scalac-plugin")).
      settings(formatSettings ++ Seq(
        name := "scalac-plugin",
        libraryDependencies ++= Seq(
          "org.scala-lang" % "scala-compiler" % scalaVersion.value % Provided,
          "org.specs2" %% "specs2-core" % specsVer.value % Test),
        compile in Test := (compile in Test).dependsOn(
          packageBin in Compile/* make sure plugin.jar is available */).value,
        sourceGenerators in Compile += Def.task[Seq[File]] {
          val dir = (sourceManaged in Compile).value

          generateUtility(scalaBinaryVersion.value, dir)
        },
        scalacOptions in Test ++= {
          val v = version.value
          val b = (baseDirectory in Compile).value
          val n = (name in Compile).value
          val msv = scalaBinaryVersion.value

          val td = b / "target" / s"scala-$msv"
          val j = td / s"${n}_$msv-$v.jar"

          Seq(s"-Xplugin:${j.getAbsolutePath}", "-P:acolyte:debug")
        }
      ))

  private def generateUtility(ver: String, managed: File): Seq[File] = {
    val f = managed / "acolyte" / "CompilerUtility.scala"
    val withSource = 
      if (ver == "2.11") "withSource(f).withShift(shift)"
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

  def compilerOptions(scalacPlugin: Project) = Def.setting[Seq[String]] {
    val v = (version in ThisBuild).value
    val b = (baseDirectory in (scalacPlugin, Compile)).value
    val n = (name in (scalacPlugin, Compile)).value
    val msv = (scalaBinaryVersion in Test).value

    val td = b / "target" / s"scala-$msv"
    val j = td / s"${n}_${msv}-$v.jar"

    Seq("-feature", "-deprecation", s"-Xplugin:${j.getAbsolutePath}")
  }
}
