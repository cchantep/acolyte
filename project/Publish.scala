import sbt._
import Keys._

object Publish {
  private val siteUrl = "http://acolyte.eu.org"

  @inline def env(n: String): String = sys.env.get(n).getOrElse(n)

  private val repoName = env("PUBLISH_REPO_NAME")
  private val repoUrl = env("PUBLISH_REPO_URL")
  private val repoId = env("PUBLISH_REPO_ID")
  private val repoUser = env("PUBLISH_USER")
  private val repoPass = env("PUBLISH_PASS")

  def settings = Seq(
    publishMavenStyle in ThisBuild := true,
    publishArtifact in Test := false,
    publishTo in ThisBuild := Some(repoUrl).map(repoName at _),
    credentials in ThisBuild ++= Seq(
      Credentials(repoName, repoId, repoUser, repoPass)),
    pomIncludeRepository in ThisBuild := { _ => false },
    licenses in ThisBuild := Seq(
      "GNU Lesser General Public License, Version 2.1" ->
        url("https://raw.github.com/cchantep/acolyte/master/LICENSE.txt")),
    homepage in ThisBuild := Some(url(siteUrl)),
    autoAPIMappings in ThisBuild := true,
    pomExtra in ThisBuild := (
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
      </developers>
    )
  )
}
