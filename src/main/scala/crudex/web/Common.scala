package crudex.web

import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._

import scalaz.concurrent.Task

import crudex.app.Common.RunPersistence


/**
  */
object Common {

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

  private def renderResponse[A,B,E[_]](a: E[A], toHttpResponse: ToHttpResponse[A,B])(implicit effEv: RunPersistence[E], encoderEv: EntityEncoder[B]): Task[Response]  =
    toHttpResponse(effEv.runPersistEffect(a))(encoderEv)

  def renderJsonResponse[A,E[_]](a: E[A])(implicit E: RunPersistence[E], A: Encoder[A]): Task[Response] = {
    //brings evidence of EntityEncoder[A] based on Json (A: Encoder[A]) encoding evidence
    implicit val ev: EntityEncoder[A] = jsonEncoderOf[A]
    renderResponse(a, defaultToResponse[A])
  }

  def renderJsonResponseOrNotFound[A,E[_]](a: E[Option[A]])(implicit E: RunPersistence[E], A: Encoder[A]): Task[Response] = {
    //brings evidence of EntityEncoder[A] based on Json (A: Encoder[A]) encoding evidence
    implicit val ev: EntityEncoder[A] = jsonEncoderOf[A]
    renderResponse(a, optionToResponse[A])
  }

  def renderHtmlReponse[A,E[_]](a: E[A])(implicit E: RunPersistence[E], A: EntityEncoder[A]): Task[Response] =
    renderResponse(a, defaultToResponse[A])


}
