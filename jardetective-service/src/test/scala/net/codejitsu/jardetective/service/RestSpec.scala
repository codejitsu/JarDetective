package net.codejitsu.jardetective.service

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.codejitsu.jardetective.model.Model.{Dependency, Snapshot, Jar}
import org.scalatest.{Matchers, WordSpec}
import spray.json.DefaultJsonProtocol

abstract class RestSpec(service: Unit => JarDetectiveService) extends WordSpec with Matchers with ScalatestRouteTest
  with SprayJsonSupport with DefaultJsonProtocol {
  //TODO add property based tests

  val validSnapshot = Snapshot(
    jar = Jar(
      organization = "myorganization",
      name = "mymodule",
      revision = "1.2-dev"
    ),

    dependencies = List(Dependency(
      jar = Jar(organization = "testorganization",
      name = "testname",
      revision = "0.1"),
      scope = "compile"
    ))
  )

  val anotherValidSnapshot = Snapshot(
    jar = Jar(
      organization = "coolorg",
      name = "coolname",
      revision = "1.2-cool"
    ),

    dependencies = List(Dependency(
      jar = Jar(organization = "testorganization",
      name = "testname",
      revision = "0.1"),
      scope = "compile"
    ),
    Dependency(
      jar = Jar(organization = "testorganization1",
      name = "testname1",
      revision = "0.11"),
      scope = "compile"
    ))
  )

  implicit val jarFormat = jsonFormat3(Jar)
  implicit val dependencyFormat = jsonFormat2(Dependency)
  implicit val snapshotFormat = jsonFormat2(Snapshot)

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

        val module = entityAs[Snapshot]

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

        val roots = entityAs[Seq[Jar]]

        roots.nonEmpty shouldBe true

        roots.head shouldBe validSnapshot.jar
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

        val roots = entityAs[Seq[Jar]]

        roots.nonEmpty shouldBe true

        roots should contain only (validSnapshot.jar, anotherValidSnapshot.jar)
      }
    }
  }
}
