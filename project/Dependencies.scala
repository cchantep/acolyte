import sbt._
import Keys._

object Dependencies {

  val specsVer = Def.setting[String] {
    if (scalaBinaryVersion.value == "2.11") {
      "4.10.6"
    } else "4.15.0"
  }
}
