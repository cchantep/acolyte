import sbt._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._

object JdbcMcp {
  import Dependencies._

  lazy val project = Project(id = "jdbc-mcp", base = file("jdbc-mcp")).settings(
    name := "jdbc-mcp",
    libraryDependencies ++= {
      val sv = scalaBinaryVersion.value

      val jsonVer: String = {
        if (sv == "2.11") "2.7.4"
        else "2.10.7"
      }

      Seq(
        "org.specs2" %% "specs2-core" % specsVer.value % Test,
        "com.h2database" % "h2" % "2.2.224" % Test,
        "com.typesafe.play" %% "play-json" % jsonVer
      )
    },
    Test / parallelExecution := false,
    assembly / mainClass := Some("acolyte.jdbc.mcp.McpServer"),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", "versions", _*) => MergeStrategy.discard
      case x => (assembly / assemblyMergeStrategy).value(x)
    }
  )
}
