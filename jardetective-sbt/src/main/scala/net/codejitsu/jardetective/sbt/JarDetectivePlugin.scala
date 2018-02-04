package net.codejitsu.jardetective.sbt

import sbt._
import Keys._
import play.api.libs.json.Json
import net.codejitsu.jardetective.model.Model._

object JarDetectivePlugin extends AutoPlugin {
  implicit val moduleFormat = Json.format[Jar]
  implicit val dependencyFormat = Json.format[Dependency]
  implicit val snapshotFormat = Json.format[Snapshot]

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

      val jar = Jar(currentModuleOrganization, currentModuleName, currentModuleVersion)

      val dependencies = (allDependencies in Compile).value map { dep =>
        Dependency(Jar(dep.organization, dep.name, dep.revision), dep.configurations.getOrElse("compile"))
      }

      val snap = Snapshot(jar, dependencies.distinct.sortBy(d => s"${d.jar.organization}:${d.jar.name}:${d.jar.revision}"))

      printDependencies(snap)
    }
  )

  def printDependencies(snapshot: Snapshot): Unit = {
    println()
    println(s"=> ${snapshot.jar}")

    val ser = Json.toJson(snapshot)

    println(Json.prettyPrint(ser))
  }
}
