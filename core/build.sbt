name := "acolyte-core"

organization := "acolyte"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.0"

javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

autoScalaLibrary := false

scalacOptions += "-feature"

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

libraryDependencies ++= Seq("org.apache.commons" % "commons-lang3" % "3.1",
  "org.specs2" %% "specs2" % "1.14" % "test")

crossPaths := false

sourceGenerators in Compile <+= (baseDirectory in Compile) zip (sourceManaged in Compile) map { dirs =>
  val (base, managed) = dirs
  val tmpl = base / "src" / "main" / "templates" / "Row.tmpl"
  val letter = 'A' to 'Z'
  // ---
  val lim = 26
  val fs: Seq[java.io.File] = for (n <- 2 to lim) yield {
    val f = managed / "Row%d.java".format(n)
    IO.writer[java.io.File](f, "", IO.defaultCharset, false) { w =>
      val cp = for (i <- 0 until n) yield letter(i)
      val ps = for (i <- 0 until n) yield "public final %s _%d;".format(letter(i), i)
      val ip = for (i <- 0 until n) yield "final %s c%d".format(letter(i), i)
      val as = for (i <- 0 until n) yield "this._%d = c%d;".format(i, i)
      val rp = for (i <- 0 until n) yield "cs.add(this._%d);".format(i)
      val na = for (i <- 0 until n) yield "null"
      val sp = for (i <- 0 until n) yield """/**
     * Sets value for cell #%s.
     *
     * @return Updated row
     */
    public Row%s<%s> set%d(final %s value) {
      return new Row%s<%s>(%s);
    }""".format(i+1, n, cp.mkString(","), i+1, letter(i), n, cp.mkString(","), (for (j <- 0 until n) yield { if (j == i) "value" else "this._%d".format(j) }).mkString(", "))
      val hc = for (i <- 0 until n) yield "append(this._%d)".format(i)
      val eq = for (i <- 0 until n) yield "append(this._%d, other._%d)".format(i, i)
      IO.reader[Unit](tmpl) { r =>
        IO.foreachLine(r) { l =>
          w.append(l.replaceAll("#N#", n.toString).replaceAll("#CP#", cp.mkString(",")).replace("#PS#", ps.mkString("\r\n    ")).replace("#AS#", as.mkString("\r\n        ")).replace("#IP#", ip.mkString(", ")).replace("#RP#", rp.mkString("\r\n        ")).replace("#NA#", na.mkString(", ")).replace("#SP#", sp.mkString("\r\n\r\n    ")).replace("#HC#", hc.mkString(".\r\n            ")).replace("#EQ#", eq.mkString(".\r\n            "))).append("\r\n")
        }
      }
      f
    }
  }
  fs
}

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
