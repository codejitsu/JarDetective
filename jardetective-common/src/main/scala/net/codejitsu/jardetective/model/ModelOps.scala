package net.codejitsu.jardetective.model

import net.codejitsu.jardetective.model.Model.{Dependency, Module}

/**
  * Utils for Model.
  */
object ModelOps {
  implicit class ModuleLike(module: Module) {
    lazy val key: String = s"${module.organization}:${module.name}:${module.revision}"
  }

  implicit class DependencyLike(dependency: Dependency) {
    lazy val key: String = s"${dependency.organization}:${dependency.name}:${dependency.revision}:${dependency.scope}"
  }
}
