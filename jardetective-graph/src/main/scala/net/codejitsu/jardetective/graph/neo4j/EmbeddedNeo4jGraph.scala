package net.codejitsu.jardetective.graph.neo4j

import net.codejitsu.jardetective.graph.{DependencyGraph, GraphMutationResult, GraphRetrievalResult, RootsRetrievalResult}
import net.codejitsu.jardetective.model.Model.{DependencySnapshot, Module}

import scala.concurrent.Future

trait EmbeddedNeo4jGraph extends DependencyGraph {
  override def addOrUpdateSnapshot(snapshot: DependencySnapshot): Future[GraphMutationResult] = Future.failed(new RuntimeException)
  override def lookUpOutDependencies(module: Module): Future[GraphRetrievalResult] = Future.failed(new RuntimeException)
  override def lookUpRoots(module: Module): Future[RootsRetrievalResult] = Future.failed(new RuntimeException)
}
