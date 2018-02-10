package net.codejitsu.jardetective.graph.remote

import net.codejitsu.jardetective.graph.{DependencyGraph, GraphMutationResult, GraphRetrievalResult, RootsRetrievalResult}
import net.codejitsu.jardetective.model.Model.{Dependency, Jar, Snapshot}

import scala.concurrent.Future

trait RemoteNeo4jGraph extends DependencyGraph {
  override def addOrUpdateSnapshot(snapshot: Snapshot): Future[GraphMutationResult] = ???

  override def lookUpOutDependencies(module: Jar): Future[GraphRetrievalResult] = ???

  override def lookUpRoots(dependency: Dependency): Future[RootsRetrievalResult] = ???
}
