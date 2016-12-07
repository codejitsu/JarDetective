import sbt._
import Keys._

object BuildSettings {

  lazy val basicSettings = seq(
    version               := "0.1.0-SNAPSHOT",
    organization          := "net.codejitsu",
    startYear             := Some(2016),
    licenses              := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalaVersion          := "2.11.8",
    resolvers             ++= Dependencies.resolutionRepos
  )

  lazy val jarDetectiveSbtSettings = basicSettings
  lazy val jarDetectiveServiceSettings = basicSettings
}
