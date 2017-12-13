package net.codejitsu.jardetective.graph.mock

import net.codejitsu.jardetective.graph._
import net.codejitsu.jardetective.model.Model.{Dependency, DependencySnapshot, Module}
import net.codejitsu.jardetective.model.ModelOps._

import scala.concurrent.Future

/**
  * Mock graph.
  */
trait MockDependencyGraph extends DependencyGraph {
  import scala.collection.mutable.Map

  val graph = Map.empty[String, Seq[Dependency]]

  override def addOrUpdateSnapshot(snapshot: DependencySnapshot): Future[GraphMutationResult] = {
    graph.update(snapshot.module.key, snapshot.dependencies)

    Future.successful(GraphMutationSuccess)
  }

  override def lookUpOutDependencies(module: Module): Future[GraphRetrievalResult] = {
    if (graph.contains(module.key)) {
      val snapshot = DependencySnapshot(module, graph.getOrElse(module.key, Seq.empty))
      Future.successful(GraphRetrievalSuccess(snapshot))
    } else {
      Future.successful(GraphRetrievalFailure)
    }
  }

  override def lookUpRoots(module: Module): Future[RootsRetrievalResult] = Future.successful(RootsRetrievalFailure)
}
