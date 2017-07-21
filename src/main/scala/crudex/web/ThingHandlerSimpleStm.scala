package crudex.web

import scalaz._
import Scalaz._
import scalaz.concurrent.Task

import org.http4s._
import org.http4s.dsl._
import org.http4s.circe._

import io.circe._
import io.circe.syntax._

import crudex.model._
import crudex.persist.stm.ThingStm._

/**
  * Simple very explicit version of CRUD handler with poor code reuse.
  */
case class ThingHandlerSimpleStm(initStore: ThingStore) {
  import io.circe.generic.auto._
  import crudex.model.instances._

  def runInStm[A]: ThingStmEff[A] => Task[A] = runAtomicallyWithStore[A](initStore)

  val thingService = HttpService {
    case GET -> Root / "things" / IntVar(thingId) =>
      for {
        res <- runInStm(getThing(ThingId(thingId))
        )
        rep <- res match {
          case Some(thing) => Ok(thing.asJson)
          case None => NotFound()
        }
      } yield (rep)

    case GET -> Root / "things" =>
        for {
           things <- runInStm(getThings)
           rep <- Ok(things.asJson)
        } yield (rep)

    case req @ POST -> Root / "things" =>
      for {
        thing <- req.as(jsonOf[Thing])
        res  <-
          runInStm(
            createThing(thing)
          )
        rep <- Ok(res.asJson)
      } yield (rep)

    case req @ PUT -> Root / "things"/ IntVar(thingId) =>
      for {
        thing <- req.as(jsonOf[Thing])
        res  <- runInStm(modifyThing(ThingId(thingId))(thing))
        rep  <- Ok(res.asJson)
      } yield (rep)

    case DELETE -> Root / "things" / IntVar(thingId) =>
        runInStm(
          deleteThing(ThingId(thingId))
        ) >>= (_ => Ok(().asJson))

  }
}
