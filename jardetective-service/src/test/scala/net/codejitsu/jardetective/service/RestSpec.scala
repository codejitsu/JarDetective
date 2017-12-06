package net.codejitsu.jardetective.service

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.codejitsu.jardetective.graph.mock.MockDependencyGraph
import net.codejitsu.jardetective.model.Model.{Dependency, DependencySnapshot, Module}
import org.scalatest.{Matchers, WordSpec}

class RestSpec extends WordSpec with Matchers with ScalatestRouteTest {
  //TODO add property based tests
  //TODO add graph service injection

  import de.heikoseeberger.akkahttpcirce.CirceSupport._
  import io.circe.generic.auto._

  val validSnapshot = DependencySnapshot(
    module = Module(
      organization = "myorganization",
      name = "mymodule",
      revision = "1.2-dev"
    ),

    dependencies = List(Dependency(
      organization = "testorganization",
      name = "testname",
      revision = "0.1",
      scope = "compile"
    ))
  )

  val service = new JarDetectiveService with MockDependencyGraph

  "The Jar Detective service" should {
    "return 201 (Created) for POST requests on /dependencies endpoint" in {
      Post("/dependencies", validSnapshot) ~> service.route ~> check {
        status shouldBe StatusCodes.Created
      }
    }

    "return 404 (Not Found) for GET requests on /dependencies endpoint for an unknown artifact" in {
      Get("/dependencies/mymodule/myorganization/1.2-dev/") ~> service.route ~> check {
        status shouldBe StatusCodes.NotFound
        entityAs[String].isEmpty shouldBe true
      }
    }
/*
    "return 200 (OK) with entity for GET requests on /module endpoint for an known artifact" in {
      Post("/snapshot", validSnapshot) ~> service.route ~> check {
        status shouldBe StatusCodes.Created
      }

      Get("/module/mymodule/myorganization/1.2-dev/") ~> service.route ~> check {
        status shouldBe StatusCodes.OK
        entityAs[String].nonEmpty shouldBe true

        val module = entityAs[DependencySnapshot]

        module shouldBe equal(validSnapshot)
      }
    }
*/
    // /parents/com.google.guava/guava/19.0/
  }
}
