import sbt._
import Keys._

class JdbcScala(
  jdbcDriver: Project,
  scalacPlugin: Project) {

  import Dependencies._
  import Format._

  lazy val project = 
    Project(id = "jdbc-scala", base = file("jdbc-scala")).
      settings(formatSettings).settings(
        name := "jdbc-scala",
        javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
        /*scalacOptions in Test ++= {
          val v = (version in ThisBuild).value
          val sv = (scalaVersion in ThisBuild).value
          val b = (baseDirectory in (scalacPlugin, Compile)).value
          val n = (name in (scalacPlugin, Compile)).value

          val msv = {
            if (sv startsWith "2.10") "2.10"
            else if (sv startsWith "2.11") "2.11"
            else if (sv startsWith "2.12") "2.12"
            else sv
          }

          val td = b / "target" / s"scala-$msv"
          val j = td / s"${n}_${msv}-$v.jar"

          Seq("-feature", "-deprecation", s"-Xplugin:${j.getAbsolutePath}")
        },
        compile in Test := (compile in Test).
          dependsOn(compile in (scalacPlugin, Test)).value,*/
        // make sure plugin is there
        libraryDependencies ++= Seq(
          "org.eu.acolyte" % "jdbc-driver" % (version in ThisBuild).value,
          specs2Test),
        sourceGenerators in Compile += Def.task[Seq[File]] {
          val base = (baseDirectory in Compile).value
          val managed = (sourceManaged in Compile).value

          generateRowClasses(base, managed / "acolyte" / "jdbc",
            "acolyte.jdbc", false)
        }).dependsOn(/*scalacPlugin, */jdbcDriver)

  // Source generator
  private def generateRowClasses(base: File, outdir: File, pkg: String, deprecated: Boolean): Seq[File] = {
    val letterAZ = 'A' to 'Z'
    val letter = letterAZ.map(_.toString) ++: letterAZ.map(l ⇒ "A" + l)
    val lim = letter.size

    val listTmpl = base / "src" / "main" / "templates" / "RowList.tmpl"
    val rowLists: Seq[java.io.File] = for (n ← 1 to lim) yield {
      val f = outdir / s"ScalaRowList$n.scala"

      val tc = for (i ← 0 until n) yield letter(i)
      val cv = for (i ← 0 until n) yield s"val c$i: Class[${letter(i)}]"
      val ca = for (i ← 0 until n) yield s"c$i: Class[${letter(i)}]"
      val cc = for (i ← 0 until n) yield s"c$i"
      val ac = for (i ← 0 until n) yield s"list.add(c$i)"
      val vs = for (i ← 0 until n) yield letter(i).toLowerCase
      val va = for (i ← 0 until n) yield {
        s"${letter(i).toLowerCase}: ${letter(i)}"
      }

      IO.writer[java.io.File](f, "", IO.defaultCharset, false) { w ⇒
        // Generate by substitution on each line of template
        IO.reader[Unit](listTmpl) { r ⇒
          IO.foreachLine(r) { l ⇒
            w.append(l.replace("#PKG#", pkg).
              replace("#CLA#", { if (deprecated) "@deprecated" else "" }).
              replaceAll("#N#", n.toString).
              replaceAll("#TC#", tc.mkString(", ")).
              replaceAll("#CV#", cv.mkString(", ")).
              replaceAll("#CC#", cc.mkString(", ")).
              replaceAll("#AC#", ac.mkString("\r\n    ")).
              replaceAll("#VS#", vs.mkString(", ")).
              replaceAll("#VA#", va.mkString(", ")).
              replaceAll("#CA#", ca.mkString(", "))).
              append("\n")
          }
        }

        f
      }
    }

    val rlf = outdir / "RowLists.scala"
    IO.writer[java.io.File](rlf, "", IO.defaultCharset, false) { w ⇒
      val conv = Nil ++: (for (n ← 1 to lim) yield {
        val gp = (for (i ← 0 until n) yield letter(i)).mkString(", ")
        val ca = (for (i ← 0 until n) yield s"l.c$i").mkString(", ")

        s"implicit def RowList${n}AsScala[$gp](l: RowList$n.Impl[$gp]): ScalaRowList$n[$gp] = new ScalaRowList$n[$gp]($ca, l.rows, l.colNames, l.colNullables)"

      })
      val tmpl = base / "src" / "main" / "templates" / "RowLists.tmpl"

      IO.reader[Unit](tmpl) { r ⇒
        IO.foreachLine(r) { l ⇒
          w.append(l.replace("#PKG#", pkg).
            replace("#CLA#", { if (deprecated) "@deprecated" else "" }).
            replace("#SRL#", conv.mkString("\r\n  "))).
            append("\r\n")
        }
      }

      rlf
    }

    rowLists :+ rlf
  }
}
