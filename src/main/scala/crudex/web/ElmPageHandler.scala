package crudex.web

import scalaz._, Scalaz._
import org.http4s._, org.http4s.dsl._

import Common._
import crudex.model._
import crudex.persist.ElmConfigTemp._

/**
  * serves elm app
   */
object ElmPageHandler {
  //import crudex.utils.ScalatagsInstances._
  import crudex.view.ElmApp.instances._

  val elmPageService = HttpService {
    case GET -> Root / "elm"  =>
        for {
           elm <- getElmConfig
           res <- Ok(elm)
        } yield(res)

  }

}
