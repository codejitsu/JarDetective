package net.codejitsu.jardetective.graph.neo4j

import java.io.File

import net.codejitsu.jardetective.graph.{DependencyGraph, GraphMutationResult, GraphRetrievalResult, RootsRetrievalResult}
import net.codejitsu.jardetective.model.Model.{DependencySnapshot, Module}

import scala.concurrent.Future
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.io.fs.FileUtils
import java.io.IOException

import org.neo4j.graphdb.{GraphDatabaseService, RelationshipType}

trait EmbeddedNeo4jGraph extends DependencyGraph {
  lazy val databaseDirectory = new File("target/neo4j-hello-db")
  lazy val graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory)

  createDb()

  object Knows extends RelationshipType {
    override def name(): String = "Knows"
  }

  @throws[IOException]
  def createDb(): Unit = {
    FileUtils.deleteRecursively(databaseDirectory)

    registerShutdownHook(graphDb)

    try {
      val tx = graphDb.beginTx
      try {
        val firstNode = graphDb.createNode
        firstNode.setProperty("message", "Hello, ")

        val secondNode = graphDb.createNode
        secondNode.setProperty("message", "World!")

        val relationship = firstNode.createRelationshipTo(secondNode, Knows)

        relationship.setProperty("message", "brave Neo4j ")

        println(firstNode.getProperty("message"))
        println(relationship.getProperty("message"))
        println(secondNode.getProperty("message"))

        val greeting = firstNode.getProperty("message").asInstanceOf[String] + relationship.getProperty("message").asInstanceOf[String] + secondNode.getProperty("message").asInstanceOf[String]
        println(greeting)

        tx.success
      } finally if (tx != null) tx.close()
    }
  }

  def shutDown(): Unit = {
    println("Shutting down database ...")
    graphDb.shutdown
  }

  def registerShutdownHook(graphDb: GraphDatabaseService): Unit = { // Registers a shutdown hook for the Neo4j instance so that it
    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
    // running application).
    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        graphDb.shutdown()
      }
    })
  }

  override def addOrUpdateSnapshot(snapshot: DependencySnapshot): Future[GraphMutationResult] = Future.failed(new RuntimeException)
  override def lookUpOutDependencies(module: Module): Future[GraphRetrievalResult] = Future.failed(new RuntimeException)
  override def lookUpRoots(module: Module): Future[RootsRetrievalResult] = Future.failed(new RuntimeException)
}
