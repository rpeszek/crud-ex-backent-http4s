package crudex_tests.web

import crudex_tests.web.Common.{assertNone, assertSome, returnValueOf}
import crudex.model._
import crudex.persist.sql.Common.{TransactionalSqlEff, createTables}
import crudex.persist.stm.ThingStm.ThingAtomicStmEff
import crudex.web.CrudHandler

import scalaz._
import Scalaz._
import scalaz.concurrent.Task

import org.http4s._
import org.http4s.client._
import org.http4s.dsl._
import io.circe.syntax._

import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeAll}


/*
  TODO use of unsafe ==
 */
object CrudHandlerSpec extends Specification with BeforeAll{
  /* common instances */
  import io.circe.generic.auto._
  import org.http4s.circe._
  import crudex.model.instances._
  import crudex_tests.web.Common.instances._

  //region Setup Web endpoints for testing

  /* Setup CrudService for STM */
  object thingStm {
    /* Imports define type class implementations needed for EditableEntityHandler */
    import crudex.persist.stm.ThingStm.instances._

    val handler = CrudHandler[ThingId, Thing, ThingAtomicStmEff]("things")
  }

  /* Setup CrudService for Sql */
  object thingSql {
    /* Imports define type class implementations needed for EditableEntityHandler */
    import crudex.persist.sql.Common.instances._
    import crudex.persist.sql.ThingSql.instances._

    val handler = CrudHandler[ThingId, Thing, TransactionalSqlEff]("things")
  }

  //Note this will not work if I would just try to execute createTables conditionally
  //spec2 prints: Can't find a constructor for class CrudHandlerSpec
  def beforeAll = {
    createTables.unsafePerformSync
  }

  //endregion

  //region Reused helper methods etc

  private val reqThingsGET = Request(GET, uri("things"))

  /* Helper method, does GET request and finds thing in the returned list */
  private def findInThingsGetResponse(client: Client)(thing: ThingEntity): Task[Option[ThingEntity]] =
     for {
       returnedThings <- client.expect[List[ThingEntity]](reqThingsGET.uri)
     } yield(returnedThings.find(_ == thing))

  //endregion

  /* data used in tests */
  val createdThing = Thing("testNm", "testDesc", None)
  val modifiedThing = Thing("testModNm", "testModDesc", None)

  val tests: Seq[(Client, String)] =
    Seq(
      (Client.fromHttpService(thingStm.handler.crudService), "STM"),
      (Client.fromHttpService(thingSql.handler.crudService), "SQL")
    )

  tests foreach { test =>
    val testNm = test._2
    val client = test._1
    val findInThingsGet = findInThingsGetResponse(client)(_)

    s"Crud Service ${testNm}" should {
      "do full CRUD" in {
        {
          {
            for {
              createdEntity <- client.expect[ThingEntity](POST(uri("things"), createdThing.asJson))
              maybeCreatedInList <- findInThingsGet(createdEntity)
              createdEntityVerified <- assertSome(maybeCreatedInList, "created not in get list")
              createdId = createdEntityVerified.id.id.toString

              modThing <- client.expect[Thing](PUT(uri("things") / createdId, modifiedThing.asJson))
              modEntity = createdEntityVerified.copy(entity = modThing)
              maybeModInList <- findInThingsGet(modEntity)
              modThingVerified <- assertSome(maybeModInList, "modified not in list")

              _ <- client.expect[Unit](DELETE(uri("things") / createdId))
              maybeDeletedInList <- findInThingsGet(modEntity)
              _ <- assertNone(maybeDeletedInList, "deleted not in list")
            } yield (())
          }: Task[Unit]
        } must returnValueOf(())
      }

      //TODO these need better typing
      "respond 200 to get things" in {
        client.get(reqThingsGET.uri) {
          case Ok(resp) => Task.now(Some(Ok))
          case _ => Task.now(None)
        } must returnValueOf(Some(Ok))
      }

      "respond 404 to GET things/invalidID" in {
        client.get(Request(GET, uri("things") / "-1").uri) {
          case NotFound(_) => Task.now(Some(NotFound))
          case _ => Task.now(None)
        } must returnValueOf(Some(NotFound))
      }

      //TODO some thinking needed how to better type this
      // String is convenient here as it also works as payload type
      "respond 404 to MODIFY things/invalidID" in {
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

      "respond 404 to DELETE things/invalidID" in {
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



}
