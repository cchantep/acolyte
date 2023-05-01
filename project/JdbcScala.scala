import sbt._
import Keys._

final class JdbcScala(jdbcDriver: Project) {

  import Dependencies._

  lazy val project =
    Project(id = "jdbc-scala", base = file("jdbc-scala"))
      .settings(
        name := "jdbc-scala",
        // make sure plugin is there
        libraryDependencies ++= Seq(
          "org.eu.acolyte" % "jdbc-driver" % (ThisBuild / version).value,
          "org.specs2" %% "specs2-core" % specsVer.value % Test
        ),
        Compile / sourceGenerators += Def.task[Seq[File]] {
          val base = (Compile / baseDirectory).value
          val managed = (Compile / sourceManaged).value

          generateRowClasses(
            base,
            managed / "acolyte" / "jdbc",
            "acolyte.jdbc",
            false
          )
        }
      )
      .dependsOn(jdbcDriver)

  // Source generator
  private def generateRowClasses(
      base: File,
      outdir: File,
      pkg: String,
      deprecated: Boolean
    ): Seq[File] = {
    val letterAZ = 'A' to 'Z'
    val letter = letterAZ.map(_.toString) ++: letterAZ.map(l => "A" + l)
    val lim = letter.size

    val listTmpl = base / "src" / "main" / "templates" / "RowList.tmpl"
    val rowLists: Seq[java.io.File] = for (n <- 1 to lim) yield {
      val f = outdir / s"ScalaRowList$n.scala"

      val tc = for (i <- 0 until n) yield letter(i)
      val cv = for (i <- 0 until n) yield s"val c$i: Class[${letter(i)}]"
      val ca = for (i <- 0 until n) yield s"c$i: Class[${letter(i)}]"
      val cc = for (i <- 0 until n) yield s"c$i"
      val ac = for (i <- 0 until n) yield s"list.add(c$i)"
      val vs = for (i <- 0 until n) yield letter(i).toLowerCase
      val va = for (i <- 0 until n) yield {
        s"${letter(i).toLowerCase}: ${letter(i)}"
      }

      IO.writer[java.io.File](f, "", IO.defaultCharset, false) { w =>
        // Generate by substitution on each line of template
        IO.reader[Unit](listTmpl) { r =>
          IO.foreachLine(r) { ln =>
            val l = {
              if (n == 1) {
                ln.replace(
                  "#EXTRA#",
                  s"""
  @inline def :+(v: A): ScalaRowList1[A] = append(v)
"""
                )
              } else {
                ln.replace("#EXTRA#", "")
              }
            }

            w.append(
              l.replace("#PKG#", pkg)
                .replace("#CLA#", { if (deprecated) "@deprecated" else "" })
                .replaceAll("#N#", n.toString)
                .replaceAll("#TC#", tc.mkString(", "))
                .replaceAll("#CV#", cv.mkString(", "))
                .replaceAll("#CC#", cc.mkString(", "))
                .replaceAll("#AC#", ac.mkString("\r\n    "))
                .replaceAll("#VS#", vs.mkString(", "))
                .replaceAll("#VA#", va.mkString(", "))
                .replaceAll("#CA#", ca.mkString(", "))
            ).append("\n")
          }
        }

        f
      }
    }

    val rlf = outdir / "RowLists.scala"
    IO.writer[java.io.File](rlf, "", IO.defaultCharset, false) { w =>
      val conv = Nil ++: (for (n <- 1 to lim) yield {
        val gp = (for (i <- 0 until n) yield letter(i)).mkString(", ")
        val ca = (for (i <- 0 until n) yield s"l.c$i").mkString(", ")

        s"  implicit def rowList${n}AsScala[$gp](l: RowList$n.Impl[$gp]): ScalaRowList$n[$gp] = new ScalaRowList$n[$gp]($ca, l.rows, l.colNames, l.colNullables, l.cycling)\r\n"

      })
      val tmpl = base / "src" / "main" / "templates" / "RowLists.tmpl"

      IO.reader[Unit](tmpl) { r =>
        IO.foreachLine(r) { l =>
          w.append(
            l.replace("#PKG#", pkg)
              .replace("#CLA#", { if (deprecated) "@deprecated" else "" })
              .replace("#SRL#", conv.mkString("\r\n  "))
          ).append("\r\n")
        }
      }

      rlf
    }

    rowLists :+ rlf
  }
}
