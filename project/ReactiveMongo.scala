import sbt._
import Keys._

trait ReactiveMongo { deps: Dependencies ⇒
  def scalacPlugin: Project

  val reactiveMongoVer = settingKey[String]("Reactive Mongo version")

  // Shared dependency
  val reactiveMongoLib = "org.reactivemongo" %% "reactivemongo"
  val reactiveResolvers = Seq(
    "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/")

  private lazy val reactiveMongoGen =
    Project(id = "reactive-mongo-sbt", 
      base = file("project") / "reactive-mongo-sbt").settings(
      name := "reactive-mongo-sbt",
      javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
      autoScalaLibrary := false,
      scalacOptions += "-feature",
      resolvers ++= reactiveResolvers,
      reactiveMongoVer in ThisBuild := {
        if (scalaVersion.value startsWith "2.11") "0.10.5.akka23-SNAPSHOT"
        else "0.10.0"
      },
      libraryDependencies ++= Seq(
        reactiveMongoLib % reactiveMongoVer.value, 
        "org.javassist" % "javassist" % "3.18.2-GA")
    )

  lazy val generatedClassDirectory = settingKey[File](
    "Directory where classes get generated")

  lazy val generatedClasses = taskKey[Seq[(File, String)]]("Generated classes")

  lazy val reactiveMongo =
    Project(id = "reactive-mongo", base = file("reactive-mongo")).settings(
      name := "reactive-mongo",
      javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
      autoScalaLibrary := false,
      scalacOptions <++= (version in ThisBuild).
        zip(scalaVersion in ThisBuild).
        zip(baseDirectory in (scalacPlugin, Compile)).
        zip(name in (scalacPlugin, Compile)) map { d =>
          val (((v, sv), b), n) = d
          val msv = 
            if (sv startsWith "2.10") "2.10" 
            else if (sv startsWith "2.11") "2.11" 
            else sv

          val td = b / "target" / s"scala-$msv"
          val j = td / s"${n}_${msv}-$v.jar"

          Seq("-feature", "-deprecation", s"-Xplugin:${j.getAbsolutePath}")
        },
      resolvers ++= reactiveResolvers,
      libraryDependencies ++= Seq(
        reactiveMongoLib % reactiveMongoVer.value, 
        "com.jsuereth" %% "scala-arm" % "1.4",
        specs2Test),
      generatedClassDirectory := {
        val dir = target.value / "generated_classes"
        if (!dir.exists) dir.mkdirs()
        dir
      },
      generatedClasses <<= Def.task {
        val cp = (fullClasspath in (reactiveMongoGen, Compile)).value
        val cl = classpath.ClasspathUtilities.toLoader(cp.files)
        val genClass = cl loadClass "acolyte.reactivemongo.ActorSystemGenerator"
        val generator = genClass.newInstance.
          asInstanceOf[{def writeTo(out: File): Array[File] }]
        val outdir = generatedClassDirectory.value
        val generated: Array[File] = generator writeTo outdir

        generated.foldLeft(Seq[(File, String)]()) { (s, f) =>
          val path = f.getAbsolutePath

          s :+ (f -> path.
            drop(outdir.getAbsolutePath.length+1))

        }
      } dependsOn(compile in (reactiveMongoGen, Compile)),
      managedClasspath in Compile := {
        val cp = (managedClasspath in Compile).value
        cp :+ Attributed.blank(generatedClassDirectory.value)
      },
      managedClasspath in Test := { // Same for test
        val cp = (managedClasspath in Test).value
        cp :+ Attributed.blank(generatedClassDirectory.value)
      },
      compile in Compile <<= (compile in Compile) dependsOn generatedClasses,
      mappings in (Compile, packageBin) := {
        val ms = mappings.in(Compile, packageBin).value
        ms ++ generatedClasses.value // add generated classes to package
      }
    ).dependsOn(reactiveMongoGen, scalacPlugin)
}
