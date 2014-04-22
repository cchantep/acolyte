import sbt._
import Keys._

trait ScalacPlugin { deps: Dependencies ⇒
  lazy val scalacPlugin = 
    Project(id = "scalac-plugin", base = file("scalac-plugin")).settings(
      name := "scalac-plugin",
      javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value, specs2Test),
      compile in Test <<= (compile in Test).dependsOn(
        packageBin in Compile/* make sure plugin.jar is available */),
      scalacOptions in Compile ++= Seq("-feature", "-deprecation"),
      scalacOptions in Test <++= (version in ThisBuild).
        zip(scalaVersion in ThisBuild).zip(baseDirectory in Compile).
        zip(name in Compile) map { d =>
          val (((v, sv), b), n) = d
          val msv = 
            if (sv startsWith "2.10") "2.10" 
            else if (sv startsWith "2.11") "2.11" 
            else sv

          val td = b / "target" / "scala-%s".format(msv)
          val j = td / "%s_%s-%s.jar".format(n, msv, v)

          Seq("-feature", "-deprecation", "-P:acolyte:debug",
            "-Xplugin:%s".format(j.getAbsolutePath))
        })

}
