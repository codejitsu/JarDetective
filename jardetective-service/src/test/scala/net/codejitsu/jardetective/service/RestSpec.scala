package net.codejitsu.jardetective.service

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.codejitsu.jardetective.model.Model.{Dependency, DependencySnapshot, Module}
import org.scalatest.{Matchers, WordSpec}
import spray.json.DefaultJsonProtocol

abstract class RestSpec(service: Unit => JarDetectiveService) extends WordSpec with Matchers with ScalatestRouteTest
  with SprayJsonSupport with DefaultJsonProtocol {
  //TODO add property based tests

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

  val anotherValidSnapshot = DependencySnapshot(
    module = Module(
      organization = "coolorg",
      name = "coolname",
      revision = "1.2-cool"
    ),

    dependencies = List(Dependency(
      organization = "testorganization",
      name = "testname",
      revision = "0.1",
      scope = "compile"
    ),
    Dependency(
      organization = "testorganization1",
      name = "testname1",
      revision = "0.11",
      scope = "compile"
    ))
  )

  implicit val dependencyFormat = jsonFormat4(Dependency)
  implicit val moduleFormat = jsonFormat3(Module)
  implicit val snapshotFormat = jsonFormat2(DependencySnapshot)

  "The Jar Detective service" should {
    "return 201 (Created) for POST requests on /dependencies endpoint" in {
      Post("/dependencies", validSnapshot) ~> service().route ~> check {
        status shouldBe StatusCodes.Created
      }
    }

    "return 404 (Not Found) for GET requests on /dependencies endpoint for an unknown artifact" in {
      val url = s"/dependencies/${UUID.randomUUID().toString}/mymodule/1.2-dev/"
      Get(url) ~> service().route ~> check {
        status shouldBe StatusCodes.NotFound
        entityAs[String].isEmpty shouldBe true
      }
    }

    "return 200 (OK) with entity for GET requests on /dependencies endpoint for an known artifact" in {
      val srv = service()

      Post("/dependencies", validSnapshot) ~> srv.route ~> check {
        status shouldBe StatusCodes.Created
      }

      Get("/dependencies/myorganization/mymodule/1.2-dev/") ~> srv.route ~> check {
        status shouldBe StatusCodes.OK
        entityAs[String].nonEmpty shouldBe true

        val module = entityAs[DependencySnapshot]

        module shouldBe validSnapshot
      }
    }

    "return 404 (NotFound) for GET requests on /roots endpoint for an unknown artifact" in {
      Get(s"/roots/myorganization/mymodule/${UUID.randomUUID().toString}/compile") ~> service().route ~> check {
        status shouldBe StatusCodes.NotFound
        entityAs[String].isEmpty shouldBe true
      }
    }

    "return 200 (OK) for GET requests on /roots endpoint for an known artifact" in {
      val srv = service()

      Post("/dependencies", validSnapshot) ~> srv.route ~> check {
        status shouldBe StatusCodes.Created
      }

      Get("/roots/testorganization/testname/0.1/compile") ~> srv.route ~> check {
        status shouldBe StatusCodes.OK

        entityAs[String].nonEmpty shouldBe true

        val roots = entityAs[Seq[Module]]

        roots.nonEmpty shouldBe true

        roots.head shouldBe validSnapshot.module
      }
    }

    "return 200 (OK) for GET requests on /roots endpoint for some known artifacts" in {
      val srv = service()

      Post("/dependencies", validSnapshot) ~> srv.route ~> check {
        status shouldBe StatusCodes.Created
      }

      Post("/dependencies", anotherValidSnapshot) ~> srv.route ~> check {
        status shouldBe StatusCodes.Created
      }

      Get("/roots/testorganization/testname/0.1/compile") ~> srv.route ~> check {
        status shouldBe StatusCodes.OK

        entityAs[String].nonEmpty shouldBe true

        val roots = entityAs[Seq[Module]]

        roots.nonEmpty shouldBe true

        roots should contain only (validSnapshot.module, anotherValidSnapshot.module)
      }
    }
  }
}
