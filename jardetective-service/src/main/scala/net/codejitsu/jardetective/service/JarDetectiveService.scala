package net.codejitsu.jardetective.service

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.stream.ActorMaterializer
import net.codejitsu.jardetective.graph._
import net.codejitsu.jardetective.model.Model.{DependencySnapshot, Module}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class JarDetectiveService {
  self: DependencyGraph =>

   val route = {
     import de.heikoseeberger.akkahttpcirce.CirceSupport._
     import akka.http.scaladsl.server.Directives._
     import io.circe.generic.auto._

     path("dependencies") {

       // POST /snapshots
       // {
       //   "module": {
       //     "name": ...
       //   }
       // }

       // 201 - snapshot stored
       // 400 - invalid input

       post {
         decodeRequest {
           entity(as[DependencySnapshot]) { snapshot =>
             // add snapshot to the graph
             onComplete(addOrUpdateSnapshot(snapshot)) {
               case Success(result) => result match {
                 case GraphMutationSuccess =>
                   complete(
                     StatusCodes.Created,
                     s"Snapshot received for module: ${snapshot.module.organization}.${snapshot.module.name}-${snapshot.module.revision}"
                   )
                 case GraphMutationFailure =>
                   complete(
                     StatusCodes.BadRequest
                   )
               }
               case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
             }
           }
         }
       }
     } ~
     pathPrefix("dependencies" / Segment / Segment / Segment) { (organization, name, revision) =>
       get {
         onComplete(lookUpOutDependencies(Module(organization, name, revision))) {
           case Success(result) => result match {
             case GraphRetrievalSuccess(snapshot) => complete(StatusCodes.OK, snapshot)

             case GraphRetrievalFailure => complete(StatusCodes.NotFound, HttpEntity.Empty)
           }

           case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
         }
       }
     } ~
     pathPrefix("roots" / Segment / Segment / Segment) { (organization, name, revision) =>
       get {
         onComplete(lookUpRoots(Module(organization, name, revision))) {
           case Success(result) => result match {
             case RootsRetrievalSuccess(roots) => complete(StatusCodes.OK, roots)

             case RootsRetrievalFailure => complete(StatusCodes.NotFound, HttpEntity.Empty)
           }

           case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
         }
       }
     }
   }

   def main(args: Array[String]) {
     implicit val system = ActorSystem()
     implicit val materializer = ActorMaterializer()
     implicit val executionContext = system.dispatcher

     val log = Logging(system, this.getClass.getName)

     val (host, port) = ("0.0.0.0", 8080)

     val bindingFuture: Future[ServerBinding] =
       Http().bindAndHandle(route, host, port)

     bindingFuture.onFailure {
       case ex: Exception =>
         log.error(ex, "Failed to bind to {}:{}!", host, port)
     }
   }
}

