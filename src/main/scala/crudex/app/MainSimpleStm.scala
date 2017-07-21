package crudex.app

import crudex.persist.stm.ThingStm.initThingStore
import crudex.web.{ElmPageHandler, StaticHandler, ThingHandlerSimpleStm, UserHandler}

import org.http4s.server.{Server, ServerApp}

import scalaz.concurrent.Task
// import org.http4s.server.{Server, ServerApp}

import org.http4s.server.blaze._
import org.http4s.server.syntax._


/*
 *
 */
object MainSimpleStm extends ServerApp {

  val initStore = initThingStore.unsafePerformSync

  val services = ThingHandlerSimpleStm(initStore).thingService orElse
                 StaticHandler.staticService orElse
                 UserHandler.userService orElse
                 ElmPageHandler.elmPageService

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(services, "") //string specifies root folder
      .start
  }

}