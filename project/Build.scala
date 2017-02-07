import sbt._
import Keys._

object JarDetectiveBuild extends Build {
  import BuildSettings._
  import Dependencies._
  import Versions._

  val resolutionRepos = Seq(
    "Twitter Maven Repo" at "http://maven.twttr.com/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )

  lazy val parent = Project(id = "jardetective",
    base = file("."))
    .aggregate (jarDetectiveSbt, jarDetectiveCommon, jarDetectiveService)
    .settings(basicSettings: _*)

  lazy val jarDetectiveCommon = Project(id = "jardetective-common", base = file("jardetective-common"))
    .settings(scalaVersion := scala2_10)
    .settings(jarDetectiveCommonSettings: _*)

  lazy val jarDetectiveService = Project(id = "jardetective-service", base = file("jardetective-service"))
    .settings(jarDetectiveServiceSettings: _*)
    .settings(scalaVersion := scala2_11)
    .settings(libraryDependencies ++= jarDetectiveServiceDependencies)
    .dependsOn(jarDetectiveCommon)

  lazy val jarDetectiveSbt = Project(id = "jardetective-sbt", base = file("jardetective-sbt"))
    .settings(jarDetectiveSbtSettings: _*)
    .settings(sbtPlugin := true)
    .settings(scalaVersion := scala2_10)
    .settings(libraryDependencies ++= jarDetectiveSbtDependencies)
    .dependsOn(jarDetectiveCommon)
}
