ThisBuild / resolvers ++= Seq(
  "Tatami Snapshots" at "https://raw.github.com/cchantep/tatami/master/snapshots",
  Resolver.sonatypeCentralSnapshots
)

ThisBuild / Compile / javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

ThisBuild / Compile / doc / javacOptions --= Seq(
  "-source",
  "1.8",
  "-target",
  "1.8"
)

ThisBuild / scalaVersion := "2.12.20"

ThisBuild / crossScalaVersions := Seq(
  scalaVersion.value,
  "2.13.18",
  "3.4.3"
)

crossVersion := CrossVersion.binary

Compile / scalacOptions ++= {
  val sv = (Compile / scalaBinaryVersion).value

  if (!sv.startsWith("3.")) {
    Seq(
      "-encoding",
      "UTF-8",
      "-unchecked",
      "-deprecation",
      "-feature"
    )
  } else {
    Seq.empty
  }
}

Compile / scalacOptions ++= {
  val sv = (Compile / scalaBinaryVersion).value

  if (sv.startsWith("2.")) {
    Seq(
      "-Xfatal-warnings",
      "-Xlint",
      "-g:vars",
      "-language:higherKinds"
    )
  } else Seq.empty
}

Compile / scalacOptions ++= {
  val sv = (Compile / scalaBinaryVersion).value

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
      "-Ywarn-unused-import"
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
      "-Wunused",
      "-Wunused:imports"
    )
  } else if (sv == "3") {
    Seq("-release", "8", "-Wunused:imports", "-language:implicitConversions")
  } else {
    Seq.empty[String]
  }
}

Compile / scalacOptions ++= {
  val ver = (Compile / scalaBinaryVersion).value

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
