import sbt._
import Keys._

trait JdbcScala {
  def jdbcDriver: Project

  lazy val jdbcScala = 
    Project(id = "jdbc-scala", base = file("scala")).settings(
    name := "jdbc-scala",
    organization := "org.eu.acolyte",
    version := "1.0.14",
    scalaVersion := "2.10.3",
    javaOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    scalacOptions ++= Seq("-feature", "-deprecation"),
    resolvers += "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    libraryDependencies ++= Seq(
      "org.eu.acolyte" % "jdbc-driver" % "1.0.14",
      "org.scalaz" % "scalaz-core_2.10" % "7.0.5",
      "org.specs2" %% "specs2" % "2.3.2" % "test"),
    publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository"))),
    sourceGenerators in Compile <+= (baseDirectory in Compile) zip (sourceManaged in Compile) map (dirs ⇒ {
      val (base, managed) = dirs
      generateRowClasses(base, managed)
    }),
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
      </developers>)).dependsOn(jdbcDriver)

  // Source generator
  private def generateRowClasses(base: File, managed: File): Seq[File] = {
    val letterAZ = 'A' to 'Z'
    val letter = letterAZ.map(_.toString) ++: letterAZ.map(l ⇒ "A" + l)
    val lim = letter.size

    val listTmpl = base / "src" / "main" / "templates" / "RowList.tmpl"
    val rowLists: Seq[java.io.File] = for (n ← 1 to lim) yield {
      val f = managed / "acolyte" / "ScalaRowList%d.scala".format(n)

      val tc = for (i ← 0 until n) yield letter(i)
      val cv = for (i ← 0 until n) yield { 
        "val c%d: Class[%s]".format(i, letter(i))
      }
      val ca = for (i ← 0 until n) yield "c%d: Class[%s]".format(i, letter(i))
      val cc = for (i ← 0 until n) yield "c%d".format(i)
      val ac = for (i ← 0 until n) yield "list.add(c%d)".format(i)
      val vs = for (i ← 0 until n) yield letter(i).toLowerCase
      val va = for (i ← 0 until n) yield {
        "%s: %s".format(letter(i).toLowerCase, letter(i))
      }

      IO.writer[java.io.File](f, "", IO.defaultCharset, false) { w ⇒
        // Generate by substitution on each line of template
        IO.reader[Unit](listTmpl) { r ⇒
          IO.foreachLine(r) { l ⇒
            w.append(l.replaceAll("#N#", n.toString).
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

    val rlf = managed / "acolyte" / "RowLists.scala"
    IO.writer[java.io.File](rlf, "", IO.defaultCharset, false) { w ⇒
      val conv = Nil ++: (for (n ← 1 to lim) yield {
        val gp = (for (i ← 0 until n) yield letter(i)).mkString(", ")
        val ca = (for (i ← 0 until n) yield "l.c%d".format(i)).mkString(", ")

        "implicit def RowList%dAsScala[%s](l: RowList%d.Impl[%s]): ScalaRowList%d[%s] = new ScalaRowList%d[%s](%s, l.rows, l.colNames, l.colNullables)".format(n, gp, n, gp, n, gp, n, gp, ca)

      })
      val tmpl = base / "src" / "main" / "templates" / "RowLists.tmpl"

      IO.reader[Unit](tmpl) { r ⇒
        IO.foreachLine(r) { l ⇒
          w.append(l.replace("#SRL#", conv.mkString("\r\n  "))).
            append("\r\n")
        }
      }

      rlf
    }

    rowLists :+ rlf
  }
}
