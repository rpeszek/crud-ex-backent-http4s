package crudex_tests.web

import scalaz.IList

import scalaz.concurrent.Task

import org.http4s.client._
import org.http4s._
import org.http4s.dsl._
import org.specs2.mutable.Specification

import crudex.web.UserHandler
import crudex.persist.UserTemp._
import crudex.model._
import crudex_tests.web.Common.{returnTheSameValue, returnValueOf}

/**
  */
object UserHandlerSpec extends Specification{
  import io.circe.generic.auto._
  import org.http4s.circe._
  import crudex_tests.web.Common.instances._

  val route = UserHandler.userService
  val req = Request(GET, uri("users"))
  val client = Client.fromHttpService(route)

  "User Service" should {
    "respond 200 to get users" in {
      client.get(req.uri) {
        case Ok(resp) =>  Task.now("Ok")
        case _ => Task.now("Bad")
      } must returnValueOf("Ok")
    }

    "return all users" in {
      {
        for {
          actualUsers <- getUsers
          jsonUsers <- client.expect[List[UserEntity]](req.uri)
        } yield ((actualUsers, IList.fromList(jsonUsers)))
      } must returnTheSameValue
    }
  }



}
