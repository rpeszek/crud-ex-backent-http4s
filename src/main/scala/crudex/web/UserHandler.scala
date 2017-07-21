package crudex.web

import scalaz._
import Scalaz._

import io.circe._
import io.circe.syntax._

import org.http4s._
import org.http4s.dsl._
import org.http4s.circe._


import Common._
import crudex.persist.UserTemp._
import crudex.model._


/**
  */
object UserHandler {
  import io.circe.generic.auto._
  import crudex.model.instances._

  val userService = HttpService {
    case GET -> Root / "users" =>
      for {
         users <- getUsers
         res  <- Ok(users.asJson)
      } yield(res)
  }

}
