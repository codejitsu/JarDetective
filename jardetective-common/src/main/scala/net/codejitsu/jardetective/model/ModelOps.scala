package net.codejitsu.jardetective.model

import net.codejitsu.jardetective.model.Model.{Dependency, Jar}

/**
  * Utils for Model.
  */
object ModelOps {
  implicit class ModuleLike(jar: Jar) {
    lazy val key: String = s"${jar.organization}:${jar.name}:${jar.revision}"
  }

  implicit class DependencyLike(dependency: Dependency) {
    lazy val key: String = s"${dependency.jar.key}:${dependency.scope}"
  }
}
