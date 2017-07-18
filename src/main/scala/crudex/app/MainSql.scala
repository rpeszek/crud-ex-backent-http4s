package crudex.app

import org.http4s.server.{Server, ServerApp}
import org.http4s.server.blaze._
import org.http4s.server.syntax._

import scalaz.concurrent.Task

import crudex.model._
import crudex.persist.sql.Common._
import crudex.web._


/*
 * Uses 'generic' EditableEntityHandler to provide JSON endpoints for Entity
 */
object MainSql extends ServerApp {
  import crudex.persist.sql.Common.instances._

  /*
  Initialize H2 DB with empty table.
  */
  val _ = sqlAsDbEffect.runDbEffect(createTables)

  object thing {
    /* Imports define type class implementations needed for EditableEntityHandler */
    import io.circe.generic.auto._
    import crudex.model.instances._
    import crudex.persist.sql.Common.instances._
    import crudex.persist.sql.ThingSql.instances._

    val handler = CrudHandler[ThingId, Thing, TransactionalSqlEff]("things")
  }

  val services = ElmPageHandler.elmPageService orElse
                 StaticHandler.staticService orElse
                 UserHandler.userService //orElse
                 //thingService.crudService

  override def server(args: List[String]): Task[Server] = {
    BlazeBuilder
      .bindHttp(8080, "localhost")
      .mountService(services, "") //string specifies root folder
      .start
  }

}