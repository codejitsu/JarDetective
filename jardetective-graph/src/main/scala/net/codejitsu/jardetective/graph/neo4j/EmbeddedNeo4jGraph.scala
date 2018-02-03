package net.codejitsu.jardetective.graph.neo4j

import java.io.File

import net.codejitsu.jardetective.graph._
import net.codejitsu.jardetective.model.Model.{Dependency, DependencySnapshot, Module}

import scala.concurrent.Future
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import org.neo4j.io.fs.FileUtils

import org.neo4j.graphdb.{GraphDatabaseService, Label, Node, RelationshipType}

trait EmbeddedNeo4jGraph extends DependencyGraph {
  import net.codejitsu.jardetective.model.ModelOps._
  import EmbeddedNeo4jGraph.graphDb

  require(graphDb.isAvailable(1000))

  object Uses extends RelationshipType {
    override def name(): String = "Uses"
  }

  object UsedBy extends RelationshipType {
    override def name(): String = "UsedBy"
  }

  def addDependencyNode(dependency: Dependency, moduleNode: Node): Unit = {
    val dependencyExists = graphDb.execute(
      s"""
         | MATCH (d:Dependency) WHERE d.name = '${dependency.name}' AND d.organization = '${dependency.organization}' AND d.revision = '${dependency.revision}' AND d.scope = '${dependency.scope}' RETURN d
       """.stripMargin
    )

    if (!dependencyExists.hasNext) {
      val dependencyNode = graphDb.createNode(Label.label("Dependency"))
      dependencyNode.setProperty("name", dependency.name)
      dependencyNode.setProperty("organization", dependency.organization)
      dependencyNode.setProperty("revision", dependency.revision)
      dependencyNode.setProperty("scope", dependency.scope)

      moduleNode.createRelationshipTo(dependencyNode, Uses)
      dependencyNode.createRelationshipTo(moduleNode, UsedBy)
    }
  }

  override def addOrUpdateSnapshot(snapshot: DependencySnapshot): Future[GraphMutationResult] = {
    try {
      val tx = graphDb.beginTx()
      try {
        val moduleExists = graphDb.execute(
          s"""
             | MATCH (m: Module) WHERE m.key = '${snapshot.module.key}' RETURN m
           """.stripMargin
        )

        if (!moduleExists.hasNext) {
          val moduleNode = graphDb.createNode(Label.label("Module"))
          moduleNode.setProperty("name", snapshot.module.name)
          moduleNode.setProperty("organization", snapshot.module.organization)
          moduleNode.setProperty("revision", snapshot.module.revision)
          moduleNode.setProperty("key", snapshot.module.key)

          snapshot.dependencies.foreach { dependency =>
            addDependencyNode(dependency, moduleNode)
          }
        }

        tx.success()

        Future.successful(GraphMutationSuccess)
      } finally if (tx != null) tx.close()
    } catch {
      case th: Throwable => Future.failed(th)
    }
  }

  override def lookUpOutDependencies(module: Module): Future[GraphRetrievalResult] = {
    import scala.collection.JavaConverters._
    import scala.collection.mutable.ListBuffer

    try {
      val result = graphDb.execute(
        s"""
           | MATCH (d:Dependency) <-[Uses]- (m:Module) WHERE m.key = '${module.key}' RETURN d.name, d.organization, d.revision, d.scope
       """.stripMargin
      )

      val dependencies = new ListBuffer[Dependency]()

      while (result.hasNext()) {
        val row = result.next()

        val dependencyFields = result.columns().asScala.map(key => (key, row.get(key)))
        val dependency = dependencyFields.foldLeft(Dependency("", "", "", "")) { (dep, dat) =>
          dat._1 match {
            case "d.name" => dep.copy(name = dat._2.toString)
            case "d.organization" => dep.copy(organization = dat._2.toString)
            case "d.revision" => dep.copy(revision = dat._2.toString)
            case "d.scope" => dep.copy(scope = dat._2.toString)
          }
        }

        dependencies += dependency
      }

      if (dependencies.nonEmpty) {
        val snapshot = DependencySnapshot(module, dependencies)

        Future.successful(GraphRetrievalSuccess(snapshot))
      } else {
        Future.successful(GraphRetrievalFailure)
      }
    } catch {
      case th: Throwable =>
        println(th)
        Future.failed(th)
    }
  }

  override def lookUpRoots(module: Module): Future[RootsRetrievalResult] = Future.failed(new RuntimeException)
}

object EmbeddedNeo4jGraph {
  lazy val databaseDirectory = new File("target/neo4j-embedded")
  lazy val graphDb = {
    FileUtils.deleteRecursively(databaseDirectory)
    val db = new GraphDatabaseFactory().newEmbeddedDatabase(databaseDirectory)
    registerShutdownHook(db)

    db
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
}