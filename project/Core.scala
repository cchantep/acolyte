import sbt._
import Keys._

trait Core {
  lazy val core = Project(id = "core", base = file("core")).settings(
    name := "acolyte-core",
    organization := "acolyte",
    version := "1.0.11",
    scalaVersion := "2.10.3",
    javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    autoScalaLibrary := false,
    scalacOptions += "-feature",
    resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.1",
      "org.specs2" %% "specs2" % "2.3.2" % "test"),
    crossPaths := false,
    sourceGenerators in Compile <+= (baseDirectory in Compile) zip (sourceManaged in Compile) map (dirs ⇒ {
      val (base, managed) = dirs
      generateRowClasses(base, managed)
    }),
    publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository"))))

  // Source generator
  private def generateRowClasses(basedir: File, managedSources: File): Seq[File] = {
    val rowTmpl = basedir / "src" / "main" / "templates" / "Row.tmpl"
    val letter = ('A' to 'Z').map(_.toString) ++: ('A' to 'Z').map(l ⇒ "A" + l)
    val lim = letter.size

    val rows: Seq[java.io.File] = for (n ← 2 to lim) yield {
      val f = managedSources / "acolyte" / "Row%d.java".format(n)
      IO.writer[java.io.File](f, "", IO.defaultCharset, false) { w ⇒
        val cp = for (i ← 0 until n) yield letter(i)
        val ps = for (i ← 0 until n) yield {
          "public final %s _%d;".format(letter(i), i)
        }
        val ip = for (i ← 0 until n) yield "final %s c%d".format(letter(i), i)
        val as = for (i ← 0 until n) yield "this._%d = c%d;".format(i, i)
        val rp = for (i ← 0 until n) yield "cs.add(this._%d);".format(i)
        val na = for (i ← 0 until n) yield "null"

        val sp = for (i ← 0 until n) yield """/**
     * Sets value for cell #%s.
     *
     * @return Updated row
     */
    public Row%s<%s> set%d(final %s value) {
      return new Row%s<%s>(%s);
    }""".format(i + 1, n, cp.mkString(","), i + 1, letter(i), n, cp.mkString(","), (for (j ← 0 until n) yield { if (j == i) "value" else "this._%d".format(j) }).mkString(", "))

        val hc = for (i ← 0 until n) yield "append(this._%d)".format(i)
        val eq = for (i ← 0 until n) yield "append(this._%d, other._%d)".format(i, i)

        // Generate by substitution on each line of template
        IO.reader[Unit](rowTmpl) { r ⇒
          IO.foreachLine(r) { l ⇒
            w.append(l.replaceAll("#N#", n.toString).
              replaceAll("#CP#", cp.mkString(",")).
              replace("#PS#", ps.mkString("\n    ")).
              replace("#AS#", as.mkString("\n        ")).
              replace("#IP#", ip.mkString(", ")).
              replace("#RP#", rp.mkString("\n        ")).
              replace("#NA#", na.mkString(", ")).
              replace("#SP#", sp.mkString("\n\n    ")).
              replace("#HC#", hc.mkString(".\n            ")).
              replace("#EQ#", eq.mkString(".\n            "))).
              append("\n")

          }
        }

        f
      }
    }

    val rf = {
      val f = managedSources / "acolyte" / "Rows.java"
      IO.writer[java.io.File](f, "", IO.defaultCharset, false) { w ⇒
        w.append("""package acolyte;

import acolyte.Row.Row1;

/**
 * Rows utility/factory.
 */
public final class Rows {""")

        for (n ← 1 to lim) yield {
          val g = for (i ← 0 until n) yield letter(i)
          val p = for (i ← 0 until n) yield {
            "final %s c%d".format(letter(i), i + 1)
          }
          val a = for (i ← 0 until n) yield "c%d".format(i + 1)
          val gd = g.mkString(",")

          w.append("""

    /**
     * Creates a row with %d %s.
     */
    public static <%s> Row%d<%s> row%d(%s) {
        return new Row%d<%s>(%s);
    }""".format(n, (if (n == 1) "cell" else "cells"), gd, n, gd, n,
            p.mkString(", "), n, gd, a.mkString(", ")))
        }

        w.append("\n}")

        f
      }
    }

    val listTmpl = basedir / "src" / "main" / "templates" / "RowList.tmpl"
    val rowLists: Seq[java.io.File] = for (n ← 2 to lim) yield {
      val f = managedSources / "acolyte" / "RowList%d.java".format(n)
      val cp = for (i ← 0 until n) yield letter(i)
      val cs = for (i ← 0 until n) yield {
        "final Class<%s> c%d".format(letter(i), i)
      }
      val ic = for (i ← 0 until n) yield {
        """if (c%d == null) {
            throw new IllegalArgumentException("Invalid class for column #%d");
        }""".format(i, i)
      }
      val ac = for (i ← 0 until n) yield {
        """this.c%d = c%d;
        colClasses.add(c%d);""".format(i, i, i)
      }
      val ca = for (i ← 0 until n) yield "c%d".format(i)
      val ap = cp map { l ⇒ "final %s %s".format(l, l.toLowerCase) }
      val ps = for (i ← 0 until n) yield {
        """/**
     * Class of column #%d
     */
    private final Class<%s> c%d;""".format(i, letter(i), i)
      }

      IO.writer[java.io.File](f, "", IO.defaultCharset, false) { w ⇒
        // Generate by substitution on each line of template
        IO.reader[Unit](listTmpl) { r ⇒
          IO.foreachLine(r) { l ⇒
            w.append(l.replaceAll("#N#", n.toString).
              replaceAll("#CP#", cp.mkString(",")).
              replaceAll("#CS#", cs.mkString(", ")).
              replaceAll("#AP#", ap.mkString(", ")).
              replaceAll("#AV#", cp.map(_.toLowerCase).mkString(", ")).
              replaceAll("#PS#", ps.mkString("\n\n    ")).
              replaceAll("#IC#", ic.mkString("\n\n        ")).
              replaceAll("#AC#", ac.mkString("\n\n        ")).
              replaceAll("#CA#", ca.mkString(", "))).
              append("\n")
          }
        }

        f
      }
    }

    val facTmpl = basedir / "src" / "main" / "templates" / "RowLists.tmpl"
    val rlf: java.io.File = {
      val f = managedSources / "acolyte" / "RowLists.java"

      IO.writer[java.io.File](f, "", IO.defaultCharset, false) { w ⇒
        val funcs = (1 to lim).foldLeft(Nil: List[String]) { (l, n) ⇒
          val g = for (i ← 0 until n) yield letter(i)
          val ps = for (i ← 0 until n) yield {
            "final Class<%s> c%d".format(letter(i), i)
          }
          val cs = for (i ← 0 until n) yield {
            "final RowList.Column<%s> c%d".format(letter(i), i)
          }
          val as = for (i ← 0 until n) yield "c%d".format(i)
          val ls = for (i ← 0 until n) yield {
            "withLabel(%d, c%d.name)".format(i + 1, i)
          }
          val gp = g.mkString(",")

          l :+ """
    /**
     * Returns list of row with %d column(s).
     */
    public static <%s> RowList%d<%s> rowList%d(%s) { return new RowList%d<%s>(%s); }""".format(n, gp, n, gp, n, ps.mkString(", "), n, gp, as.mkString(", ")) :+ """
    /**
     * Returns list of row with %d column(s).
     */
    public static <%s> RowList%d<%s> rowList%d(%s) { return new RowList%d<%s>(%s.columnClass).%s; }""".format(n, gp, n, gp, n, cs.mkString(", "), n, gp, as.mkString(".columnClass, "), ls.mkString("."))

        }

        IO.reader[java.io.File](facTmpl) { r ⇒
          IO.foreachLine(r) { l ⇒
            w.append(l.replace("#F#", funcs.mkString("\n"))).append("\n")
          }
          f
        }
      }
    }

    rows ++: rowLists :+ rf :+ rlf
  }
}
