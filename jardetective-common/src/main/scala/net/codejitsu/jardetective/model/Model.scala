package net.codejitsu.jardetective.model

object Model {
  final case class Module(organization: String, name: String, revision: String)
  final case class Dependency(organization: String, name: String, revision: String, scope: String)
  final case class DependencySnapshot(module: Module, dependencies: Seq[Dependency])
}