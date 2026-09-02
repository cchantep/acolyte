resolvers ++= Seq(
  "Tatami Releases" at "https://raw.github.com/cchantep/tatami/master/releases"
)

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.1")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.6")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.5.0")

addSbtPlugin("cchantep" % "sbt-hl-compiler" % "0.12")

addSbtPlugin("cchantep" % "sbt-scaladoc-compiler" % "0.8")

addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.1.1")
