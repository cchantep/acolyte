import sbt._
import Keys._

trait ScalacPlugin {
  lazy val scalacPlugin = 
    Project(id = "scalac-plugin", base = file("scalac-plugin")).settings(
      name := "scalac-plugin",
      javaOptions ++= Seq("-source", "1.6", "-target", "1.6"),
      javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
      scalacOptions ++= Seq("-feature", "-deprecation"),
      resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % "2.10.3",
        "org.specs2" %% "specs2" % "2.3.2" % "test"),
      compile in Test <<= (compile in Test).dependsOn(
        packageBin in Compile/* make sure plugin.jar is available */),
      scalacOptions in Test <++= (version in ThisBuild).
        zip(baseDirectory in Compile).
        zip(name in Compile) map { d =>
          val ((v, b), n) = d
          val j = b / "target" / "scala-2.10" / "%s_2.10-%s.jar".format(n, v)

          Seq("-feature", "-deprecation", "-P:acolyte:debug",
            "-Xplugin:%s".format(j.getAbsolutePath))
        })

}
