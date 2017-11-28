package net.codejitsu.jardetective.graph.mock

import net.codejitsu.jardetective.graph.{DependencyGraph, GraphMutationResult, GraphMutationSuccess}
import net.codejitsu.jardetective.model.Model.DependencySnapshot

import scala.concurrent.Future

/**
  * Mock graph.
  */
trait MockDependencyGraph extends DependencyGraph {
  override def addOrUpdateSnapshot(snapshot: DependencySnapshot): Future[GraphMutationResult] = Future.successful(GraphMutationSuccess)
}
