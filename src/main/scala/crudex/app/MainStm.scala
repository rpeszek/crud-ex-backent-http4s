package crudex.app


import org.http4s.server.{Server, ServerApp}

import scalaz.concurrent.Task
// import org.http4s.server.{Server, ServerApp}

import org.http4s.server.blaze._
import org.http4s.server.syntax._

import crudex.web._
import crudex.model._
import crudex.app.Common._
import crudex.persist.stm.ThingStm._

/*
 * Uses 'generic' EditableEntityHandler to provide JSON endpoints for Entity
 */
object MainStm extends ServerApp {

  /* Imports define type class implementations needed for EditableEntityHandler */
  import io.circe.generic.auto._
  import crudex.model.instances._
  import crudex.persist.stm.ThingStm.instances._

  val thingService = CrudHandler[ThingId, Thing, StmThingDbEff]("things")

  val services = ElmPageHandler.elmPageService orElse
                 StaticHandler.staticService orElse
                 UserHandler.userService orElse
                 thingService.crudService

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(services, "") //string specifies root folder
      .start
  }

}