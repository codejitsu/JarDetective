import sbt._

object Dependencies {

  import Versions._

  val resolutionRepos = Seq(
    "Twitter Maven Repo" at "http://maven.twttr.com/",
    "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"
  )

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  val playJson = "com.typesafe.play" %% "play-json" % playJsonVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  val akkaCirce = "de.heikoseeberger" %% "akka-http-circe" % akkaCirceVersion

  val circeCore = "io.circe" %% "circe-core" % circeVersion
  val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  val circeParser = "io.circe" %% "circe-parser" % circeVersion

  val akkaTestKit = "com.typesafe.akka" %% "akka-http-testkit" % akkaTestKitVersion

  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion

  val akkaSprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpJsonVersion

  // ====================

  val jarDetectiveSbtDependencies = compile(playJson)
  val jarDetectiveServiceDependencies = compile(akkaHttp, akkaCirce, circeCore, circeGeneric, circeParser, akkaSprayJson) ++
    test(akkaTestKit, scalatest, akkaSprayJson)
  val jarDetectiveGraphDependencies = test(scalatest)
}
