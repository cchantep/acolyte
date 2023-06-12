ThisBuild / resolvers ++= (("Tatami Snapshots" at "https://raw.github.com/cchantep/tatami/master/snapshots") +: Resolver
  .sonatypeOssRepos("snapshots") ++: Resolver.sonatypeOssRepos("staging"))

ThisBuild / scalaVersion := "2.12.17"

ThisBuild / crossScalaVersions := Seq(
  "2.11.12",
  scalaVersion.value,
  "2.13.8",
  "3.3.0"
)

crossVersion := CrossVersion.binary

ThisBuild / scalacOptions ++= Seq(
  "-encoding",
  "UTF-8",
  "-unchecked",
  "-deprecation",
  "-feature"
)

ThisBuild / scalacOptions ++= {
  if (scalaBinaryVersion.value startsWith "2.") {
    Seq(
      "-Xfatal-warnings",
      "-target:jvm-1.8",
      "-Xlint",
      "-g:vars",
      "-language:higherKinds"
    )
  } else Seq.empty
}

ThisBuild / scalacOptions ++= {
  val sv = scalaBinaryVersion.value

  if (sv == "2.12") {
    Seq(
      "-Xmax-classfile-name",
      "128",
      "-Ywarn-numeric-widen",
      "-Ywarn-dead-code",
      "-Ywarn-value-discard",
      "-Ywarn-infer-any",
      "-Ywarn-unused",
      "-Ywarn-unused-import",
      "-Ywarn-macros:after"
    )
  } else if (sv == "2.11") {
    Seq(
      "-Xmax-classfile-name",
      "128",
      "-Yopt:_",
      "-Ydead-code",
      "-Yclosure-elim",
      "-Yconst-opt"
    )
  } else if (sv == "2.13") {
    Seq(
      "-explaintypes",
      "-Werror",
      "-Wnumeric-widen",
      "-Wdead-code",
      "-Wvalue-discard",
      "-Wextra-implicit",
      "-Wmacros:after",
      "-Wunused"
    )
  } else if (sv != "3") {
    Seq("-Wunused:all", "-language:implicitConversions")
  } else {
    Seq.empty[String]
  }
}

Compile / console / scalacOptions ~= {
  _.filterNot(o =>
    o.startsWith("-X") || o.startsWith("-Y") || o.startsWith("-P:silencer")
  )
}

Test / compile / scalacOptions ~= {
  val excluded = Set("-Xfatal-warnings")

  _.filterNot(excluded.contains)
}

val filteredScalacOpts: Seq[String] => Seq[String] = {
  _.filterNot { opt => opt.startsWith("-X") || opt.startsWith("-Y") }
}

Compile / console / scalacOptions ~= filteredScalacOpts

Test / console / scalacOptions ~= filteredScalacOpts

// Silencer
ThisBuild / libraryDependencies ++= {
  if (!scalaBinaryVersion.value.startsWith("3")) {
    val silencerVersion = "1.7.13"

    Seq(
      compilerPlugin(
        ("com.github.ghik" %% "silencer-plugin" % silencerVersion)
          .cross(CrossVersion.full)
      ),
      ("com.github.ghik" %% "silencer-lib" % silencerVersion % Provided)
        .cross(CrossVersion.full)
    )
  } else Seq.empty
}

Test / console / scalacOptions += "-Yrepl-class-based"

// Scaladoc
Compile / doc / scalacOptions ++= Opts.doc.title(s"Acolyte ${name.value}")

Compile / doc / scalacOptions ++= {
  sbtdynver.DynVerPlugin.autoImport.previousStableVersion.value.map {
    _.takeWhile(_ != '.')
  }.toSeq
}
