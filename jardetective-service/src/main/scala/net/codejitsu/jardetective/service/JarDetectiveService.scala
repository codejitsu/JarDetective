package net.codejitsu.jardetective.service

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.Future

object JarDetectiveService {
   def main(args: Array[String]) {
     implicit val system = ActorSystem()
     implicit val materializer = ActorMaterializer()
     implicit val executionContext = system.dispatcher

     val log = Logging(system, this.getClass.getName)

     val handler = get {
       complete("Hello world!")
     }

     val (host, port) = ("0.0.0.0", 8080)

     val bindingFuture: Future[ServerBinding] =
       Http().bindAndHandle(handler, host, port)

     bindingFuture.onFailure {
       case ex: Exception =>
         log.error(ex, "Failed to bind to {}:{}!", host, port)
     }
   }
}

