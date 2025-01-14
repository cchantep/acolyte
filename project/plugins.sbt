resolvers ++= Seq(
  "Tatami Releases" at "https://raw.github.com/cchantep/tatami/master/releases"
)

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.3")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.13.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.3.0")

addSbtPlugin("cchantep" % "sbt-hl-compiler" % "0.8")

addSbtPlugin("cchantep" % "sbt-scaladoc-compiler" % "0.2")

addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.1.0")
