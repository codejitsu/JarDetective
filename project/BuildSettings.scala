import sbt._
import Keys._

object BuildSettings {

  lazy val basicSettings = seq(
    version               := "0.1.0-SNAPSHOT",
    organization          := "net.codejitsu",
    startYear             := Some(2016),
    licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    resolvers             ++= Dependencies.resolutionRepos,
    crossScalaVersions    := Seq("2.10.0", "2.11.0")
  )

  lazy val jarDetectiveSbtSettings = basicSettings
  lazy val jarDetectiveServiceSettings = basicSettings
  lazy val jarDetectiveCommonSettings = basicSettings
}
