package net.codejitsu.jardetective.graph

import net.codejitsu.jardetective.model.Model.{Dependency, Snapshot, Jar}

import scala.concurrent.Future

sealed trait GraphMutationResult
case object GraphMutationSuccess extends GraphMutationResult
case object GraphMutationFailure extends GraphMutationResult

sealed trait GraphRetrievalResult
final case class GraphRetrievalSuccess(snapshot: Snapshot) extends GraphRetrievalResult
case object GraphRetrievalFailure extends GraphRetrievalResult

sealed trait RootsRetrievalResult
final case class RootsRetrievalSuccess(roots: Seq[Jar]) extends RootsRetrievalResult
case object RootsRetrievalFailure extends RootsRetrievalResult

/**
  * Dependency graph.
  */
trait DependencyGraph {
  def addOrUpdateSnapshot(snapshot: Snapshot): Future[GraphMutationResult]
  def lookUpOutDependencies(module: Jar): Future[GraphRetrievalResult]
  def lookUpRoots(dependency: Dependency): Future[RootsRetrievalResult]
}
