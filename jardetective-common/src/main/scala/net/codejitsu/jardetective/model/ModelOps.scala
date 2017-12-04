package net.codejitsu.jardetective.model

import net.codejitsu.jardetective.model.Model.Module

/**
  * Utils for Model.
  */
object ModelOps {
  implicit class ModuleLike(module: Module) {
    lazy val key: String = s"${module.name}:${module.organization}:${module.revision}"
  }
}
