package net.codejitsu.jardetective.graph.neo4j

import java.io.File

import net.codejitsu.jardetective.graph._
import net.codejitsu.jardetective.model.Model.{Dependency, Snapshot, Jar}

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

  def addDependencyNode(dependency: Dependency, jarNode: Node, jar: Jar): Unit = {
    val dependencyExists = graphDb.execute(
      s"""
         | MATCH (dj:Jar) <-[UsedBy]- (j:Jar)
         | WHERE dj.name = '${dependency.jar.name}' AND dj.organization = '${dependency.jar.organization}' AND dj.revision = '${dependency.jar.revision}' AND dj.scope = '${dependency.scope}'
         | AND j.key = '${jar.key}'
         | RETURN dj
       """.stripMargin
    )

    if (!dependencyExists.hasNext) {
      val dependencyJarNode = graphDb.createNode(Label.label("Jar"))
      dependencyJarNode.setProperty("name", dependency.jar.name)
      dependencyJarNode.setProperty("organization", dependency.jar.organization)
      dependencyJarNode.setProperty("revision", dependency.jar.revision)
      dependencyJarNode.setProperty("scope", dependency.scope)
      dependencyJarNode.setProperty("key", dependency.key)

      jarNode.createRelationshipTo(dependencyJarNode, Uses)
      dependencyJarNode.createRelationshipTo(jarNode, UsedBy)
    }
  }

  override def addOrUpdateSnapshot(snapshot: Snapshot): Future[GraphMutationResult] = {
    try {
      val tx = graphDb.beginTx()
      try {
        val jarExists = graphDb.execute(
          s"""
             | MATCH (j: Jar) WHERE j.key = '${snapshot.jar.key}' RETURN j
           """.stripMargin
        )

        if (!jarExists.hasNext) {
          val jarNode = graphDb.createNode(Label.label("Jar"))
          jarNode.setProperty("name", snapshot.jar.name)
          jarNode.setProperty("organization", snapshot.jar.organization)
          jarNode.setProperty("revision", snapshot.jar.revision)
          jarNode.setProperty("key", snapshot.jar.key)

          snapshot.dependencies.foreach { dependency =>
            addDependencyNode(dependency, jarNode, snapshot.jar)
          }
        }

        tx.success()

        Future.successful(GraphMutationSuccess)
      } finally if (tx != null) tx.close()
    } catch {
      case th: Throwable => Future.failed(th)
    }
  }

  override def lookUpOutDependencies(jar: Jar): Future[GraphRetrievalResult] = {
    import scala.collection.JavaConverters._
    import scala.collection.mutable.ListBuffer

    try {
      val result = graphDb.execute(
        s"""
           | MATCH (jd:Jar) <-[Uses]- (j:Jar) WHERE j.key = '${jar.key}' RETURN jd.name, jd.organization, jd.revision, jd.scope
       """.stripMargin
      )

      val dependencies = new ListBuffer[Dependency]()

      while (result.hasNext()) {
        val row = result.next()

        val dependencyFields = result.columns().asScala.map(key => (key, row.get(key)))
        val dependency = dependencyFields.foldLeft(Dependency(Jar("", "", ""), "")) { (dep, fieldValue) =>
          fieldValue._1 match {
            case "jd.name" => dep.copy(jar = dep.jar.copy(name = fieldValue._2.toString))
            case "jd.organization" => dep.copy(jar = dep.jar.copy(organization = fieldValue._2.toString))
            case "jd.revision" => dep.copy(jar = dep.jar.copy(revision = fieldValue._2.toString))
            case "jd.scope" => dep.copy(scope = fieldValue._2.toString)
          }
        }

        dependencies += dependency
      }

      if (dependencies.nonEmpty) {
        val snapshot = Snapshot(jar, dependencies)

        Future.successful(GraphRetrievalSuccess(snapshot))
      } else {
        Future.successful(GraphRetrievalFailure)
      }
    } catch {
      case th: Throwable =>
        Future.failed(th)
    }
  }

  override def lookUpRoots(dependency: Dependency): Future[RootsRetrievalResult] = {
    import scala.collection.JavaConverters._
    import scala.collection.mutable.ListBuffer

    try {
      val result = graphDb.execute(
        s"""
           | MATCH (j:Jar) <-[UsedBy]- (jd:Jar) WHERE jd.key = '${dependency.key}' RETURN j.name, j.organization, j.revision
       """.stripMargin
      )

      val jars = new ListBuffer[Jar]()

      while (result.hasNext()) {
        val row = result.next()

        val jarFields = result.columns().asScala.map(key => (key, row.get(key)))
        val jar = jarFields.foldLeft(Jar("", "", "")) { (j, fieldValue) =>
          fieldValue._1 match {
            case "j.name" => j.copy(name = fieldValue._2.toString)
            case "j.organization" => j.copy(organization = fieldValue._2.toString)
            case "j.revision" => j.copy(revision = fieldValue._2.toString)
          }
        }

        jars += jar
      }

      if (jars.nonEmpty) {
        Future.successful(RootsRetrievalSuccess(jars))
      } else {
        Future.successful(RootsRetrievalFailure)
      }
    } catch {
      case th: Throwable =>
        Future.failed(th)
    }
  }
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