import sbt._
import Keys._

object Dependencies {
  val specsVer = Def.setting[String] {
    if (scalaBinaryVersion.value == "2.10") "3.9.5" // 4.0.1 not avail
    else "4.10.0"
  }
}
