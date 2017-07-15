package crudex.utils

import crudex.model.UserId
import org.http4s._
import org.http4s.dsl._
import org.http4s.MediaType._
import org.http4s.headers._

import org.http4s.circe._
import io.circe._
import io.circe.syntax._

import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import scalaz.effect.IO


/**
  */
object Common {
  type Handler[A] = IO[A]

  /*
   Helper type defines common logic for Http response based on the rendered type.
   Converts type A to displayable type B and decides the response code

   A is the type that comes from Handler
   B is the type being rendered
   ToHttpResponse can internally convert these types, e.g. Option[A] to A
   */
  private type ToHttpResponse[A,B] = A => EntityEncoder[B] => Task[Response]
  private def optionToResponse[A]: ToHttpResponse[Option[A],A] = optA => encoderEv => {
    //converts explicit  EntityEncoder[A] evidence to implicit evidence
    implicit val ev: EntityEncoder[A] = encoderEv
    optA match {
      case Some(ax) => Ok(ax)
      case None => NotFound()
    }
  }
  private def defaultToResponse[A]: ToHttpResponse[A,A] = a => encoderEv => {
    //converts explicit  EntityEncoder[A] evidence to implicit evidence
    implicit val ev: EntityEncoder[A] = encoderEv
    Ok(a)
  }

  private def renderResponse[A,B](a: Handler[A], toHttpResponse: ToHttpResponse[A,B])(implicit encoderEv: EntityEncoder[B]): Task[Response]  =
    toHttpResponse(a.unsafePerformIO)(encoderEv)

  def toJsonResponse[A](a: Handler[A])(implicit A: Encoder[A]): Task[Response] = {
    //brings evidence of EntityEncoder[A] based on Json (A: Encoder[A]) encoding evidence
    implicit val ev: EntityEncoder[A] = jsonEncoderOf[A]
    renderResponse(a, defaultToResponse[A])
  }

  def toJsonResponseWithOption[A](a: Handler[Option[A]])(implicit A: Encoder[A]): Task[Response] = {
    //brings evidence of EntityEncoder[A] based on Json (A: Encoder[A]) encoding evidence
    implicit val ev: EntityEncoder[A] = jsonEncoderOf[A]
    renderResponse(a, optionToResponse[A])
  }

  def toScalatagsHtml[A](a: Handler[A])(implicit A: EntityEncoder[A]): Task[Response] =
    renderResponse(a, defaultToResponse[A])


}
