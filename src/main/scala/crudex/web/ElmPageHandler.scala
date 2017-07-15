package crudex.web

import scalaz._, Scalaz._
import org.http4s._, org.http4s.dsl._

import crudex.utils.Common._
import crudex.model._
import crudex.stm.ElmConfigTemp._

/**
  * serves elm app
   */
object ElmPageHandler {
  import crudex.view.ElmApp.instances._

  val elmPageService = HttpService {
    case GET -> Root / "elm"  =>
      toScalatagsHtml (
        getElmConfig
      )
  }

}
