package net.codejitsu.jardetective.graph.mock

import net.codejitsu.jardetective.graph._
import net.codejitsu.jardetective.model.Model.{Dependency, Snapshot, Jar}
import net.codejitsu.jardetective.model.ModelOps._

import scala.concurrent.Future

/**
  * Mock graph.
  */
trait MockDependencyGraph extends DependencyGraph {
  import scala.collection.mutable.Map

  val graph = Map.empty[String, Seq[Dependency]]

  override def addOrUpdateSnapshot(snapshot: Snapshot): Future[GraphMutationResult] = {
    graph.update(snapshot.jar.key, snapshot.dependencies)

    Future.successful(GraphMutationSuccess)
  }

  override def lookUpOutDependencies(module: Jar): Future[GraphRetrievalResult] = {
    if (graph.contains(module.key)) {
      val snapshot = Snapshot(module, graph.getOrElse(module.key, Seq.empty))
      Future.successful(GraphRetrievalSuccess(snapshot))
    } else {
      Future.successful(GraphRetrievalFailure)
    }
  }

  override def lookUpRoots(dependency: Dependency): Future[RootsRetrievalResult] = {
    def toModule(key: String): Jar = {
      val parts = key.split(":")

      Jar(parts(0), parts(1), parts(2))
    }

    val roots = graph.filter(r => r._2.filter(d => d == dependency).nonEmpty).keys.toSeq

    if (roots.isEmpty) {
      Future.successful(RootsRetrievalFailure)
    } else {
      Future.successful(RootsRetrievalSuccess(roots.map(toModule)))
    }
  }
}
