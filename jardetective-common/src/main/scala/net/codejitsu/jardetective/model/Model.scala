package net.codejitsu.jardetective.model

object Model {
  final case class Jar(organization: String, name: String, revision: String)
  final case class Dependency(jar: Jar, scope: String)
  final case class Snapshot(jar: Jar, dependencies: Seq[Dependency])
}