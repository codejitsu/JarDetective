package net.codejitsu.jardetective.graph.mock

import net.codejitsu.jardetective.graph._
import net.codejitsu.jardetective.model.Model.{DependencySnapshot, Module}
import net.codejitsu.jardetective.model.ModelOps._

import scala.concurrent.Future

/**
  * Mock graph.
  */
trait MockDependencyGraph extends DependencyGraph {
  import scala.collection.mutable._

  val graph = Map.empty[String, Any]

  override def addOrUpdateSnapshot(snapshot: DependencySnapshot): Future[GraphMutationResult] = Future.successful(GraphMutationSuccess)

  override def lookUpOutDependencies(module: Module): Future[GraphRetrievalResult] = {
    if (graph.contains(module.key)) {
      Future.successful(GraphRetrievalSuccess)
    } else {
      Future.successful(GraphRetrievalFailure)
    }
  }
}
