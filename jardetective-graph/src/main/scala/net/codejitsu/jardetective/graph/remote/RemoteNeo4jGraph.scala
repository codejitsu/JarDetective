package net.codejitsu.jardetective.graph.remote

import net.codejitsu.jardetective.model.ModelOps._
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import net.codejitsu.jardetective.graph._
import net.codejitsu.jardetective.model.Model.{Dependency, Jar, Snapshot}
import org.neo4j.driver.v1.{AuthTokens, GraphDatabase, Transaction}

import scala.concurrent.Future

trait RemoteNeo4jGraph extends DependencyGraph {
  val logger = Logger[RemoteNeo4jGraph]
  val config = ConfigFactory.load()

  val driver = GraphDatabase.driver(config.getString("app.graph.neo4j.url"),
    AuthTokens.basic(config.getString("app.graph.neo4j.username"), config.getString("app.graph.neo4j.password")))

  val session = driver.session()

  def addDependencyNode(tx: Transaction, dependency: Dependency, jar: Jar): Unit = {
    val dependencyExists = tx.run(
      s"""
         | MATCH (dj:Jar) <-[:UsedBy]- (j:Jar)
         | WHERE dj.name = '${dependency.jar.name}' AND dj.organization = '${dependency.jar.organization}' AND dj.revision = '${dependency.jar.revision}' AND dj.scope = '${dependency.scope}'
         | AND j.key = '${jar.key}'
         | RETURN dj
       """.stripMargin
    )

    if (!dependencyExists.hasNext) {
      val scriptDependencyJar = s"CREATE (jar:Jar) SET jar.name = '${dependency.jar.name}', jar.organization = '${dependency.jar.organization}', jar.revision = '${dependency.jar.revision}', jar.key = '${dependency.key}', jar.scope = '${dependency.scope}'"
      tx.run(scriptDependencyJar)

      val scriptUses =
        s"""
           | MATCH (jar: Jar),(usedJar: Jar)
           | WHERE jar.key = '${jar.key}' AND usedJar.key = '${dependency.key}'
           | CREATE (jar)-[:Uses]->(usedJar)""".stripMargin
      tx.run(scriptUses)

      val scriptUsedBy =
        s"""
           | MATCH (dependendJar: Jar), (jar: Jar)
           | WHERE dependendJar.key = '${dependency.key}' AND jar.key = '${jar.key}'
           | CREATE (dependendJar)-[:UsedBy]->(jar)""".stripMargin
      tx.run(scriptUsedBy)
    }
  }

  override def addOrUpdateSnapshot(snapshot: Snapshot): Future[GraphMutationResult] = {
    try {
      val tx = session.beginTransaction()

      try {
        val jarExists = tx.run(
          s"""
             | MATCH (j: Jar) WHERE j.key = '${snapshot.jar.key}' RETURN j
         """.stripMargin
        )

        if (!jarExists.hasNext) {
          val scriptJarNode = s"CREATE (jar:Jar) SET jar.name = '${snapshot.jar.name}', jar.organization = '${snapshot.jar.organization}', jar.revision = '${snapshot.jar.revision}', jar.key = '${snapshot.jar.key}'"
          tx.run(scriptJarNode)

          snapshot.dependencies.foreach { dependency =>
            addDependencyNode(tx, dependency, snapshot.jar)
          }
        }

        tx.success()
        Future.successful(GraphMutationSuccess)
      } catch {
        case th: Throwable =>
          logger.error(th.getMessage, th)

          Option(tx).foreach { t =>
            t.close()
          }

          Future.failed(th)
      } finally {
        Option(tx).foreach { t =>
          t.close()
        }
      }
    } catch {
      case th: Throwable =>
        logger.error(th.getMessage, th)
        Future.failed(th)
    }
  }

  override def lookUpOutDependencies(jar: Jar): Future[GraphRetrievalResult] = {
    import scala.collection.JavaConverters._
    import scala.collection.mutable.ListBuffer

    try {
      val result = session.run(
        s"""
           | MATCH (jd:Jar) <-[:Uses]- (j:Jar) WHERE j.key = '${jar.key}' RETURN jd.name, jd.organization, jd.revision, jd.scope
       """.stripMargin
      )

      val dependencies = new ListBuffer[Dependency]()

      while (result.hasNext()) {
        val row = result.next()

        println(row)

        val dependencyFields = result.keys().asScala.map(key => (key, row.get(key)))
        val dependency = dependencyFields.foldLeft(Dependency(Jar("", "", ""), "")) { (dep, fieldValue) =>
          println((dep, fieldValue) )

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
        th.printStackTrace()
        Future.failed(th)
    }
  }

  override def lookUpRoots(dependency: Dependency): Future[RootsRetrievalResult] = ???
}
