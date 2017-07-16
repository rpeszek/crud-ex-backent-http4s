package crudex.web

import scalaz._, Scalaz._
import org.http4s._, org.http4s.dsl._
import org.http4s.circe._

import crudex.persist.stm.ThingStm._
import io.circe._
import io.circe.syntax._

import crudex.web.Common._
import crudex.app.Common._
import io.circe.Encoder

/**
  * //TODO needs evConvertKey
  */
case class CrudHandler[K,D,E[_]](uri: String)(implicit evM: Monad[E],
                                              evRunPersist: RunDbEffect[E],
                                              evConvertKey: IntId[K],
                                              evPersist: CrudDb[K,D,E],
                                              evJsonK: Encoder[K],
                                              evJsonD: Encoder[D],
                                              evDecodeJsonD: Decoder[D]) {

  import io.circe.generic.auto._  //needed for auto json instance of Entity
  import crudex.model.instances._

  val crudService = HttpService {
    case GET -> Root / uri / IntVar(thingId) =>
      renderJsonResponseOrNotFound (
        evPersist.retrieveRecord(evConvertKey.fromInt(thingId))
      )

    case GET -> Root / uri =>
      renderJsonResponse(
        evPersist.retrieveAll
      )

    case req @ POST -> Root / uri =>
      for {
        thing <- req.as(jsonOf[D])
        res  <- renderJsonResponse(evPersist.create(thing))
      } yield (res)

    case req @ PUT -> Root / uri / IntVar(thingId) =>
      for {
        thing <- req.as(jsonOf[D])
        res  <- renderJsonResponse(evPersist.update(evConvertKey.fromInt(thingId))(thing))
      } yield (res)

    case DELETE -> Root / uri / IntVar(thingId) =>
      renderJsonResponse(
        evPersist.delete(evConvertKey.fromInt(thingId))
      )
  }
}
