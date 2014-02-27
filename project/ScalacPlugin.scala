import sbt._
import Keys._

trait ScalacPlugin { deps: Dependencies ⇒
  lazy val scalacPlugin = 
    Project(id = "scalac-plugin", base = file("scalac-plugin")).settings(
      name := "scalac-plugin",
      javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
      resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % "2.10.3", specs2Test),
      compile in Test <<= (compile in Test).dependsOn(
        packageBin in Compile/* make sure plugin.jar is available */),
      scalacOptions in Compile ++= Seq("-feature", "-deprecation"),
      scalacOptions in Test <++= (version in ThisBuild).
        zip(baseDirectory in Compile).
        zip(name in Compile) map { d =>
          val ((v, b), n) = d
          val j = b / "target" / "scala-2.10" / "%s_2.10-%s.jar".format(n, v)

          Seq("-feature", "-deprecation", "-P:acolyte:debug",
            "-Xplugin:%s".format(j.getAbsolutePath))
        })

}
