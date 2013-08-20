name := "acolyte-scala"

organization := "acolyte"

version := "1.0.6"

scalaVersion := "2.10.2"

javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

scalacOptions ++= Seq("-feature", "-deprecation")

sourceGenerators in Compile <+= (baseDirectory in Compile) zip (sourceManaged in Compile) map (dirs ⇒ {
  val (base, managed) = dirs
  val tmpl = base / "src" / "main" / "templates" / "RowLists.tmpl"
  val f = managed / "acolyte" / "RowLists.scala"
  IO.writer[Seq[java.io.File]](f, "", IO.defaultCharset, false) { w ⇒
    val letter = 'A' to 'Z'
    val lim = letter.size
    val conv = for (n ← 1 to lim) yield {
      val gp = (for (i ← 0 until n) yield letter(i)).mkString(", ")
      """implicit def RowList%dAsScala[%s](l: RowList%d[%s]): ScalaRowList[RowList%d[%s], Row%d[%s]] = new ScalaRowList[RowList%d[%s], Row%d[%s]](l)""".format(n, gp, n, gp, n, gp, n, gp, n, gp, n, gp)
    }
    IO.reader[Unit](tmpl) { r ⇒
      IO.foreachLine(r) { l ⇒
        w.append(l.replace("#SRL#", conv.mkString("\r\n  "))).
          append("\r\n")
      }
    }
    Seq(f)
  }
})

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

libraryDependencies ++= Seq(
  "acolyte" % "acolyte-core" % "1.0.6",
  "org.specs2" %% "specs2" % "1.14" % "test")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
