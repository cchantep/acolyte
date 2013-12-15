name := "acolyte-scala"

organization := "acolyte"

version := "1.0.11"

scalaVersion := "2.10.3"

javaOptions ++= Seq("-source", "1.6", "-target", "1.6")

javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

scalacOptions ++= Seq("-feature", "-deprecation")

sourceGenerators in Compile <+= (baseDirectory in Compile) zip (sourceManaged in Compile) map (dirs ⇒ {
  val (base, managed) = dirs
  val rlf = managed / "acolyte" / "RowLists.scala"
  IO.writer[java.io.File](rlf, "", IO.defaultCharset, false) { w ⇒
    val letter = ('A' to 'Z').map(_.toString) ++: ('A' to 'Z').map(l ⇒ "A" + l)
    val lim = letter.size
    val conv = for (n ← 1 to lim) yield {
      val gp = (for (i ← 0 until n) yield letter(i)).mkString(", ")
      """implicit def RowList%dAsScala[%s](l: RowList%d[%s]): ScalaRowList[RowList%d[%s], Row%d[%s]] = new ScalaRowList[RowList%d[%s], Row%d[%s]](l)""".format(n, gp, n, gp, n, gp, n, gp, n, gp, n, gp)
    }
    val tmpl = base / "src" / "main" / "templates" / "RowLists.tmpl"
    IO.reader[Unit](tmpl) { r ⇒
      IO.foreachLine(r) { l ⇒
        w.append(l.replace("#SRL#", conv.mkString("\r\n  "))).
          append("\r\n")
      }
    }
    rlf
  }
  val rf = managed / "acolyte" / "Rows.scala"
  IO.writer[java.io.File](rf, "", IO.defaultCharset, false) { w ⇒
    val letter = 'A' to 'Z' dropRight 4
    val conv = for (n ← 1 to 22) yield {
      val gp = (for (i ← 0 until n) yield letter(i)).mkString(", ")
      val ps = for (i ← 1 to n) yield "p._%d".format(i)
      """implicit def Product%dAsRow[%s](p: Product%d[%s]): Row%d[%s] = Rows.row%d(%s)""".format(n, gp, n, gp, n, gp, n, ps.mkString(", "))
    }
    val tmpl = base / "src" / "main" / "templates" / "Rows.tmpl"
    IO.reader[Unit](tmpl) { r ⇒
      IO.foreachLine(r) { l ⇒
        w.append(l.replace("#SR#", conv.mkString("\r\n  "))).
          append("\r\n")
      }
    }
    rf
  }
  Seq(rlf, rf)
})

resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"

libraryDependencies ++= Seq(
  "acolyte" % "acolyte-core" % "1.0.11",
  "org.specs2" %% "specs2" % "2.3.2" % "test")

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
  <developers>
    <developer>
      <id>cchantep</id>
      <name>Cedric Chantepie</name>
    </developer>
  </developers>)

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))
