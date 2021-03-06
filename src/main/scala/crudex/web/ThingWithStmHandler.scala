package crudex.web

import scalaz._, Scalaz._
import org.http4s._, org.http4s.dsl._
import org.http4s.circe._

import Common._
import crudex.persist.stm.ThingStm._
import io.circe._
import io.circe.syntax._
import crudex.model._
//import org.http4s.server.syntax._

/**
  * For demonstraction only, replaced by more generic CrudHandler
  * Hardcodes Thing as entity and STM as DB effect
  * See CrudHandler for more polymorphic take on this.
  */
object ThingWithStmHandler {
  //Just loads JSON!
  import io.circe.generic.auto._
  import crudex.model.instances._
  import crudex.persist.stm.ThingStm.instances._

  val thingService = HttpService {
    case GET -> Root / "things" / IntVar(thingId) =>
      renderJsonResponseOrNotFound (
        runAtomically(
         getThing(ThingId(thingId))
        )
      )

    case GET -> Root / "things" =>
      renderJsonResponse(
        runAtomically(
          getThings
        )
      )

    case req @ POST -> Root / "things" =>
      for {
        thing <- req.as(jsonOf[Thing])
        res  <- renderJsonResponse(
          runAtomically(
            createThing(thing)
          )
        )
      } yield (res)

    case req @ PUT -> Root / "things"/ IntVar(thingId) =>
      for {
        thing <- req.as(jsonOf[Thing])
        res  <- renderJsonResponseOrNotFound(runAtomically(modifyThing(ThingId(thingId))(thing)))
      } yield (res)

    case DELETE -> Root / "things" / IntVar(thingId) =>
      renderJsonResponse(
        runAtomically(
          deleteThing(ThingId(thingId))
        )
      )

  }
}
