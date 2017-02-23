package net.codejitsu.jardetective.service

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import net.codejitsu.jardetective.model.Model.{Dependency, DependencySnapshot, Module}
import org.scalatest.{Matchers, WordSpec}

class RestSpec extends WordSpec with Matchers with ScalatestRouteTest {
  //TODO add property based tests
  //TODO add graph service injection

  import JarDetectiveService.route
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

  "The Jar Detective service" should {
    "return 201 (Created) for POST requests on /snapshots endpoint" in {
      Post("/snapshots", validSnapshot) ~> route ~> check {
        status shouldBe equal(StatusCodes.Created)
      }
    }
  }
}
