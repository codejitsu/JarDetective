package net.codejitsu.jardetective.graph

import net.codejitsu.jardetective.model.Model.{DependencySnapshot, Module}

import scala.concurrent.Future

sealed trait GraphMutationResult
case object GraphMutationSuccess extends GraphMutationResult
case object GraphMutationFailure extends GraphMutationResult

sealed trait GraphRetrievalResult
final case class GraphRetrievalSuccess(snapshot: DependencySnapshot) extends GraphRetrievalResult
case object GraphRetrievalFailure extends GraphRetrievalResult

sealed trait RootsRetrievalResult
final case class RootsRetrievalSuccess(roots: Seq[Module]) extends RootsRetrievalResult
case object RootsRetrievalFailure extends RootsRetrievalResult

/**
  * Dependency graph.
  */
trait DependencyGraph {
  def addOrUpdateSnapshot(snapshot: DependencySnapshot): Future[GraphMutationResult]
  def lookUpOutDependencies(module: Module): Future[GraphRetrievalResult]
  def lookUpRoots(module: Module): Future[RootsRetrievalResult]
}
