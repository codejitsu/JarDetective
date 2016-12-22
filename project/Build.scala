import sbt._
import Keys._

object JarDetectiveBuild extends Build {
  import BuildSettings._
  import Dependencies._

  val resolutionRepos = Seq(
    "Twitter Maven Repo" at "http://maven.twttr.com/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )

  lazy val parent = Project(id = "jardetective",
    base = file("."))
    .aggregate (jarDetectiveSbt, jarDetectiveCommon, jarDetectiveService)
    .settings(basicSettings: _*)

  lazy val jarDetectiveCommon = Project(id = "jardetective-common", base = file("jardetective-common"))
    .settings(scalaVersion := "2.10.4")
    .settings(jarDetectiveCommonSettings: _*)

  lazy val jarDetectiveService = Project(id = "jardetective-service", base = file("jardetective-service"))
    .settings(jarDetectiveServiceSettings: _*)

  lazy val jarDetectiveSbt = Project(id = "jardetective-sbt", base = file("jardetective-sbt"))
    .settings(jarDetectiveSbtSettings: _*)
    .settings(sbtPlugin := true)
    .settings(scalaVersion := "2.10.4")
    .settings(libraryDependencies ++= jarDetectiveSbtDependencies)
    .dependsOn(jarDetectiveCommon)
}
