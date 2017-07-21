package crudex.web

import scalaz._
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

import scalaz.concurrent.Task


/**
  * Defines common methods used by Handler objects
  */
object Common {


//  /*
//  Public methods used by handler objects
//  */
//  def renderJsonResponse(a: Task[Response])(implicit  A: Encoder[A]): Task[Response] = {
//    //brings evidence of EntityEncoder[A] based on Json (A: Encoder[A]) encoding evidence
//    implicit val ev: EntityEncoder[A] = jsonEncoderOf[A]
//    a
//  }
//
//
//
//  def renderHtmlReponse[A](a: Task[A])(implicit encoderEv: EntityEncoder[A]): Task[Response] = {
//     Ok(a)
//  }


}
