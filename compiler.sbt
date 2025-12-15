ThisBuild / resolvers ++= (("Tatami Snapshots" at "https://raw.github.com/cchantep/tatami/master/snapshots") +: Resolver
  .sonatypeOssRepos("snapshots") ++: Resolver.sonatypeOssRepos("staging"))

ThisBuild / scalaVersion := "2.12.21"

ThisBuild / crossScalaVersions := Seq(
  "2.11.12",
  scalaVersion.value,
  "2.13.15",
  "3.4.2"
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
      "-target:jvm-1.8",
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
      "-target:jvm-1.8",
      "-Xmax-classfile-name",
      "128",
      "-Yopt:_",
      "-Ydead-code",
      "-Yclosure-elim",
      "-Yconst-opt"
    )
  } else if (sv == "2.13") {
    Seq(
      "-release",
      "8",
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
    Seq("-release", "8", "-Wunused:all", "-language:implicitConversions")
  } else {
    Seq.empty[String]
  }
}

ThisBuild / scalacOptions ++= {
  val ver = scalaBinaryVersion.value

  if (ver == "2.13") {
    Seq(
      "-Wconf:msg=.*inferred\\ to\\ be.*(Any|AnyVal|Object).*:is"
    )
  } else {
    Seq.empty
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
libraryDependencies ++= {
  val v = scalaBinaryVersion.value

  if (!v.startsWith("3")) {
    val silencerVersion: String = {
      if (v == "2.11") {
        "1.17.13"
      } else {
        "1.7.19"
      }
    }

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
