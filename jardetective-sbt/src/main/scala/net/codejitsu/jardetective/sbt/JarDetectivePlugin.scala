package net.codejitsu.jardetective.sbt

import sbt._
import Keys._

object JarDetectivePlugin extends AutoPlugin {
  final case class Dependency(organization: String, name: String, revision: String)

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
      val dependencies = (allDependencies in Compile).value map { dep =>
        Dependency(dep.organization, dep.name, dep.revision)
      }

      processDependencies(dependencies.distinct.sortBy(d => s"${d.organization}:${d.name}:${d.revision}"))
    }
  )

  def processDependencies(deps: Seq[Dependency]): Unit = {
    deps.foreach(println)
  }
}
