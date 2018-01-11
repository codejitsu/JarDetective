import sbt._
import Keys._

object BuildSettings {
  import Versions._

  lazy val basicSettings = Seq(
    version               := "0.1.0-SNAPSHOT",
    organization          := "net.codejitsu",
    startYear             := Some(2016),
    licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    resolvers             ++= Dependencies.resolutionRepos,
    crossScalaVersions    := Seq(scala2_10, scala2_11)
  )

  lazy val jarDetectiveSbtSettings = basicSettings
  lazy val jarDetectiveServiceSettings = basicSettings
  lazy val jarDetectiveCommonSettings = basicSettings
  lazy val jarDetectiveGraphSettings = basicSettings
}
