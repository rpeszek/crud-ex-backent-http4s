package crudex_tests.web

import crudex_tests.web.Common.{assertNone, assertSome}
import crudex.model._
import crudex.persist.stm.ThingStm._

import crudex.web.{CrudHandler}
import crudex_tests.web.Common.{returnTheSameValue, returnValueOf}

import scalaz._, Scalaz._
import org.http4s._
import org.http4s.client._
import org.http4s.dsl._

import io.circe.syntax._

import org.specs2.mutable.Specification

import scalaz.concurrent.Task

/**
  */
object CrudHandlerSpec extends Specification{
  import crudex_tests.web.Common.instances._
  import io.circe.generic.auto._
  import crudex.model.instances._
  import org.http4s.circe._

  object thingStm {
    /* Imports define type class implementations needed for EditableEntityHandler */
    import crudex.persist.stm.ThingStm.instances._

    val handler = CrudHandler[ThingId, Thing, ThingAtomicStmEff]("things")
  }

  val route = thingStm.handler.crudService
  val client = Client.fromHttpService(route)

  val reqThingsGET = Request(GET, uri("things"))

  /* Helper method, does GET requests and looks finds thing in the returned list */
  private def findInGetThingsResponse(thing: ThingEntity): Task[Option[ThingEntity]] =
     for {
       returnedThings <- client.expect[List[ThingEntity]](reqThingsGET.uri)
     } yield(returnedThings.find(_ == thing))


  val testThing = Thing("testNm", "testDesc", None)
  val modifiedThing = Thing("testModNm", "testModDesc", None)

  "Crud Service" should {
    "do full CRUD" in {
       {
         {
           for {
             createdEntity           <- client.expect[ThingEntity](POST(uri("things"), testThing.asJson))
             maybeCreatedInList      <- findInGetThingsResponse(createdEntity)
             createdEntityVerified   <- assertSome(maybeCreatedInList, "created not in get list")
             createdId  = createdEntityVerified.id.id.toString

             modThing                <- client.expect[Thing](PUT(uri("things") / createdId, modifiedThing.asJson))
             modEntity =  createdEntityVerified.copy(entity = modThing)
             maybeModInList          <- findInGetThingsResponse(modEntity)
             modThingVerified        <- assertSome(maybeModInList, "modified not in list")

             _                       <- client.expect[Unit](DELETE(uri("things") / createdId))
             maybeDeletedInList      <- findInGetThingsResponse(modEntity)
             _                       <- assertNone(maybeDeletedInList, "deleted not in list")
           } yield(())
         }: Task[Unit]
       } must returnValueOf(())
    }

    "respond 200 to get things" in {
      client.get(reqThingsGET.uri) {
        case Ok(resp) =>  Task.now("Ok")
        case _ => Task.now("Bad")
      } must returnValueOf("Ok")
    }

    "responds 404 to GET things/invalidID" in {
      client.get(Request(GET, uri("things")/"-1").uri) {
        case NotFound(_) =>  Task.now("NotFound")
        case _ => Task.now("Bad")
      } must returnValueOf("NotFound")
    }

    "responds 404 to MODIFY things/invalidID" in {
      {
        {
          client.expect[String](PUT(uri("things") / "-1", modifiedThing.asJson)).handle {
            case e: UnexpectedStatus =>
              if (e.status == NotFound) "NotFound" else "Bad"
            case _ => "Bad"
          }
        }: Task[String]
      } must returnValueOf("NotFound")
    }

    "responds 404 to DELETE things/invalidID" in {
      {
        {
          client.expect[String](DELETE(uri("things") / "-1")).handle {
            case e: UnexpectedStatus =>
              if (e.status == NotFound) "NotFound" else "Bad"
            case _ => "Bad"
          }
        }: Task[String]
      } must returnValueOf("NotFound")
    }

  }



}
