import sbt._
import Keys._

trait Studio {
  lazy val studio = 
    Project(id = "studio", base = file("studio"), settings = pomSettings)

  private val pom = (baseDirectory in Compile) { base ⇒
    import scala.xml.XML
    val pomFile = base / "pom.xml"
    XML.loadFile(pomFile)
  }

  private val pomSettings = Project.defaultSettings ++ Seq(
    organization <<= pom { _ \\ "project" \ "groupId" text },
    name <<= pom { _ \\ "project" \ "artifactId" text },
    version <<= pom { _ \\ "project" \ "version" text },
    javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    scalacOptions += "-feature",
    crossPaths := false,
    resolvers <<= pom {
      _ \\ "project" \ "repositories" \ "repository" map (r ⇒
        (r \ "name").text at (r \ "url").text)
    },
    externalResolvers += Resolver.mavenLocal,
    publishTo := Some(Resolver.file("file", new File(
      Path.userHome.absolutePath+"/.m2/repository")))
  ) ++ externalPom( /*dependencies*/ )
}
