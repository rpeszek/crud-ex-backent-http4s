package crudex.web

import scalaz._, Scalaz._
import org.http4s._, org.http4s.dsl._
import org.http4s.circe._

import crudex.utils.Common._
import crudex.stm.ThingStm._
import io.circe._
import io.circe.syntax._
import crudex.model._
//import org.http4s.server.syntax._

/**
  */
object ThingHandler {
  val thingStoreM = initThingStore

  //Just loads JSON!
  import io.circe.generic.auto._
  import crudex.model.instances._

  val thingService = HttpService {
    case GET -> Root / "things" / IntVar(thingId) =>
      renderJsonResponseOrNotFound (
        thingStoreM >>=  getThing(ThingId(thingId))
      )

    case GET -> Root / "things" =>
      renderJsonResponse(
        thingStoreM >>= getThings
      )

    case req @ POST -> Root / "things" =>
      for {
        thing <- req.as(jsonOf[Thing])
        res  <- renderJsonResponse(thingStoreM >>=  createThing(thing))
      } yield (res)

    case req @ PUT -> Root / "things"/ IntVar(thingId) =>
      for {
        thing <- req.as(jsonOf[Thing])
        res  <- renderJsonResponse(thingStoreM >>= modifyThing(ThingId(thingId))(thing))
      } yield (res)

    case DELETE -> Root / "things" / IntVar(thingId) =>
      renderJsonResponse(
        thingStoreM >>= deleteThing(ThingId(thingId))
      )

  }
}
