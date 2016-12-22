package net.codejitsu.jardetective.sbt

import sbt._
import Keys._
import play.api.libs.json.Json

object JarDetectivePlugin extends AutoPlugin {
  implicit val moduleFormat = Json.format[Module]
  implicit val dependencyFormat = Json.format[Dependency]
  implicit val snapshotFormat = Json.format[DependencySnapshot]

  final case class Module(organization: String, name: String, revision: String)
  final case class Dependency(organization: String, name: String, revision: String, scope: String)
  final case class DependencySnapshot(module: Module, dependencies: Seq[Dependency])

  object autoImport {
    /**
      * Configuration for the plugin.
      */
    lazy val JarDetective = config("jardetective") extend (Compile)

    lazy val printJson = taskKey[Unit]("Prints json of all project dependencies.")
    lazy val snapshot = taskKey[Unit]("Takes snapshot of all project dependencies.")
  }

  import autoImport._

  override def trigger = allRequirements

  override def projectSettings = Seq(
    printJson in JarDetective := {
      val currentModuleName = moduleName.value
      val currentModuleOrganization = organization.value
      val currentModuleVersion = version.value

      val module = Module(currentModuleOrganization, currentModuleName, currentModuleVersion)

      val dependencies = (allDependencies in Compile).value map { dep =>
        Dependency(dep.organization, dep.name, dep.revision, dep.configurations.getOrElse("compile"))
      }

      val snap = DependencySnapshot(module, dependencies.distinct.sortBy(d => s"${d.organization}:${d.name}:${d.revision}"))

      printDependencies(snap)
    }
  )

  def printDependencies(snapshot: DependencySnapshot): Unit = {
    println()
    println(s"=> ${snapshot.module}")

    val ser = Json.toJson(snapshot)

    println(Json.prettyPrint(ser))
  }
}
