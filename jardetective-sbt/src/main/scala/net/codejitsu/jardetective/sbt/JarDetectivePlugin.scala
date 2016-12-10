package net.codejitsu.jardetective.sbt

import sbt._
import Keys._

object JarDetectivePlugin extends AutoPlugin {
  final case class Module(organization: String, name: String, revision: String)
  final case class Dependency(organization: String, name: String, revision: String, scope: String)

  object autoImport {
    /**
      * Configuration for the plugin.
      */
    lazy val JarDetective = config("jardetective") extend (Compile)

    lazy val snapshot = taskKey[Unit]("Takes snapshot of all project dependencies.")
  }

  import autoImport._

  override def trigger = allRequirements

  override def projectSettings = Seq(
    snapshot in JarDetective := {
      val currentModuleName = moduleName.value
      val currentModuleOrganization = organization.value
      val currentModuleVersion = version.value

      val module = Module(currentModuleOrganization, currentModuleName, currentModuleVersion)

      val dependencies = (allDependencies in Compile).value map { dep =>
        Dependency(dep.organization, dep.name, dep.revision, dep.configurations.getOrElse("compile"))
      }

      processDependencies(module, dependencies.distinct.sortBy(d => s"${d.organization}:${d.name}:${d.revision}"))
    }
  )

  def processDependencies(module: Module, deps: Seq[Dependency]): Unit = {
    //println(s"Current module: ${currentModule.organization}:${currentModule.name}:${currentModule.revision}")
    println()
    println(module)
    println("=============================================")

    deps.foreach(println)
  }
}
