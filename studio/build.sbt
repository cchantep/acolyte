name := "acolyte-studio"

organization := "org.eu.acolyte"

version := "1.0.11"

scalaVersion := "2.10.3"

javacOptions in Test ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

autoScalaLibrary := false

scalacOptions += "-feature"

resolvers += "Applicius Releases Repository" at "https://raw.github.com/applicius/mvn-repo/master/releases/"

libraryDependencies ++= Seq(
  "melasse" % "melasse-core" % "1.0",
  "de.sciss" % "syntaxpane" % "1.1.0")

crossPaths := false

publishTo := Some(Resolver.file("file", new File(Path.userHome.absolutePath + "/.m2/repository")))

pomExtra := (
      <url>https://github.com/cchantep/acolyte/</url>
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
      </developers>)
