package net.codejitsu.jardetective.graph

import net.codejitsu.jardetective.model.Model.DependencySnapshot

import scala.concurrent.Future

sealed trait GraphMutationResult
case object GraphMutationSuccess extends GraphMutationResult
case object GraphMutationFailure extends GraphMutationResult

/**
  * Dependency graph.
  */
trait DependencyGraph {
  def addOrUpdateSnapshot(snapshot: DependencySnapshot): Future[GraphMutationResult]
}
