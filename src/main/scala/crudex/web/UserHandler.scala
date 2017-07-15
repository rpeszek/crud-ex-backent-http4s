package crudex.web

import scalaz._, Scalaz._
import org.http4s._, org.http4s.dsl._
import org.http4s.circe._

import crudex.utils.Common._
import crudex.stm.UserTemp._
import crudex.model._

/**
  * Created by rpeszek on 7/12/17.
  */
object UserHandler {
  import io.circe.generic.auto._
  import crudex.model.instances._

  val userService = HttpService {
    case GET -> Root / "users" =>
      toJsonResponse(
        getUsers
      )
  }

}
