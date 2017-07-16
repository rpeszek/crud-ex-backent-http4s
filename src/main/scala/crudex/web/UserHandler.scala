package crudex.web

import scalaz._, Scalaz._
import org.http4s._, org.http4s.dsl._
import org.http4s.circe._

import Common._
import crudex.persist.UserTemp._
import crudex.model._

/**
  * Created by rpeszek on 7/12/17.
  */
object UserHandler {
  import io.circe.generic.auto._
  import crudex.model.instances._
  import crudex.app.Common.instances._

  val userService = HttpService {
    case GET -> Root / "users" =>
      renderJsonResponse(
        getUsers
      )
  }

}
