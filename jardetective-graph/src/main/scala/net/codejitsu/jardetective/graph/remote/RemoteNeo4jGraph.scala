package net.codejitsu.jardetective.graph.remote

import net.codejitsu.jardetective.model.ModelOps._
import com.typesafe.config.ConfigFactory
import net.codejitsu.jardetective.graph._
import net.codejitsu.jardetective.model.Model.{Dependency, Jar, Snapshot}
import org.neo4j.driver.v1.{AuthTokens, GraphDatabase, Transaction}

import scala.concurrent.Future

trait RemoteNeo4jGraph extends DependencyGraph {
  val config = ConfigFactory.load()

  val driver = GraphDatabase.driver(config.getString("app.graph.neo4j.url"),
    AuthTokens.basic(config.getString("app.graph.neo4j.username"), config.getString("app.graph.neo4j.password")))

  val session = driver.session()

  def addDependencyNode(tx: Transaction, dependency: Dependency, jar: Jar): Unit = {
    val dependencyExists = tx.run(
      s"""
         | MATCH (dj:Jar) <-[UsedBy]- (j:Jar)
         | WHERE dj.name = '${dependency.jar.name}' AND dj.organization = '${dependency.jar.organization}' AND dj.revision = '${dependency.jar.revision}' AND dj.scope = '${dependency.scope}'
         | AND j.key = '${jar.key}'
         | RETURN dj
       """.stripMargin
    )

    if (!dependencyExists.hasNext) {
      val scriptDependencyJar = s"CREATE (jar:Jar {name:'${dependency.jar.name}',organization:'${dependency.jar.organization}',revision:'${dependency.jar.revision}',key:'${dependency.key}',scope:'${dependency.scope}'})"
      tx.run(scriptDependencyJar)

      val dependencyKeys = dependency.key

      val scriptUses = s"MATCH (jar: Jar {key: '${jar.key}'}) FOREACH (key in ['${dependencyKeys}'] | CREATE (jar)-[:Uses]->(:Jar {key:key}))"
      tx.run(scriptUses)

      val scriptUsedBy = s"MATCH (jar: Jar {key: '${dependency.key}'}) FOREACH (key in ['${jar.key}'] | CREATE (jar)-[:UsedBy]->(:Jar {key:key}))"
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
          val scriptJarNode = s"CREATE (jar:Jar {name:'${snapshot.jar.name}',organization:'${snapshot.jar.organization}',revision:'${snapshot.jar.revision}',key:'${snapshot.jar.key}'})"
          tx.run(scriptJarNode)

          snapshot.dependencies.foreach { dependency =>
            addDependencyNode(tx, dependency, snapshot.jar)
          }
        }

        tx.success()
        Future.successful(GraphMutationSuccess)
      } catch {
        case th: Throwable =>
          Option(tx).foreach { t =>
            t.close()
          }

          th.printStackTrace()

          Future.failed(th)
      } finally {
        Option(tx).foreach { t =>
          t.close()
        }
      }
    } catch {
      case th: Throwable =>
        th.printStackTrace()

        Future.failed(th)
    }
  }

  override def lookUpOutDependencies(module: Jar): Future[GraphRetrievalResult] = ???

  override def lookUpRoots(dependency: Dependency): Future[RootsRetrievalResult] = ???
}
