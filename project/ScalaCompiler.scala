import sbt._
import Keys._

trait ScalaCompiler {
  lazy val scalaCompiler = 
    Project(id = "scala-compiler", base = file("scala-compiler")).
    aggregate(plugin, specs).settings(
      version in ThisBuild := "1.0.14")

  private lazy val plugin = Project(
    id = "scala-compiler-plugin", 
    base = file("scala-compiler/plugin")).settings(
      name := "scala-compiler-plugin",
      organization := "org.eu.acolyte",
      scalaVersion := "2.10.3",
      javaOptions ++= Seq("-source", "1.6", "-target", "1.6"),
      javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
      scalacOptions ++= Seq("-feature", "-deprecation"),
      resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.10.3",
      publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository"))),
      pomExtra := (
      <url>https://github.com/cchantep/acolyte/</url>
      <licenses>
        <license>
          <name>GNU Lesser General Public License, Version 2.1</name>
          <url>
            https://raw.github.com/cchantep/acolyte/master/LICENSE.txt
          </url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:git@github.com:cchantep/acolyte.git</connection>
        <developerConnection>
          scm:git:git@github.com:cchantep/acolyte.git
        </developerConnection>
        <url>git@github.com:cchantep/acolyte.git</url>
      </scm>
      <issueManagement>
        <system>GitHub</system>
        <url>https://github.com/cchantep/acolyte/issues</url>
      </issueManagement>
      <ciManagement>
        <system>Travis CI</system>
        <url>https://travis-ci.org/cchantep/acolyte</url>
      </ciManagement>
      <developers>
        <developer>
          <id>cchantep</id>
          <name>Cedric Chantepie</name>
        </developer>
      </developers>))

  private lazy val specs = Project(
    id = "scala-compiler-specs", 
    base = file("scala-compiler/specs")).
    settings(
      name := "scala-compiler-specs",
      organization := "org.eu.acolyte",
      scalaVersion := "2.10.3",
      javaOptions ++= Seq("-source", "1.6", "-target", "1.6"),
      javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
      compile in Test <<= (compile in Test).dependsOn(clean in Test).dependsOn(
        packageBin in (plugin, Compile)/* make sure plugin.jar is available */),
      scalacOptions in Test <++= (version in ThisBuild).
        zip(baseDirectory in (plugin, Compile)).
        zip(name in (plugin, Compile)) map { d =>
          val ((v, b), n) = d
          val j = b / "target" / "scala-2.10" / "%s_2.10-%s.jar".format(n, v)

          Seq("-feature", "-deprecation", "-P:acolyte:debug",
            "-Xplugin:%s".format(j.getAbsolutePath))
        },
      resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
      libraryDependencies ++= Seq("org.specs2" %% "specs2" % "2.3.2" % "test"),
      publishTo := None).dependsOn(plugin)

}
