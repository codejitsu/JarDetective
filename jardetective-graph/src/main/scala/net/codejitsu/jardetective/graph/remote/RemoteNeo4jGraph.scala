package net.codejitsu.jardetective.graph.remote

import com.typesafe.config.ConfigFactory
import net.codejitsu.jardetective.graph.{DependencyGraph, GraphMutationResult, GraphRetrievalResult, RootsRetrievalResult}
import net.codejitsu.jardetective.model.Model.{Dependency, Jar, Snapshot}
import org.neo4j.driver.v1.{AuthToken, AuthTokens, GraphDatabase}

import scala.concurrent.Future

trait RemoteNeo4jGraph extends DependencyGraph {
  val config = ConfigFactory.load()

  val driver = GraphDatabase.driver(config.getString("app.graph.neo4j.url"),
    AuthTokens.basic(config.getString("app.graph.neo4j.username"), config.getString("app.graph.neo4j.password")))

  val session = driver.session()

  override def addOrUpdateSnapshot(snapshot: Snapshot): Future[GraphMutationResult] = ???

  override def lookUpOutDependencies(module: Jar): Future[GraphRetrievalResult] = ???

  override def lookUpRoots(dependency: Dependency): Future[RootsRetrievalResult] = ???
}
