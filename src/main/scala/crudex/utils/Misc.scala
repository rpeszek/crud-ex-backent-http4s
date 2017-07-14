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
object Misc {
  type Handler[A] = IO[A]

  //TODO Temp
  def toJsonResponse[A](a: Handler[A])(implicit A: Encoder[A]): Task[Response] = {
    Ok(a.unsafePerformIO.asJson)
  }

  def toJsonResponseList[A](a: Handler[IList[A]])(implicit A: Encoder[List[A]]): Task[Response] = {
    Ok(a.unsafePerformIO.toList.asJson)
  }

  def toJsonResponseWithOption[A](a: Handler[Option[A]])(implicit A: Encoder[A]): Task[Response] = {
    val maybeA:Option[A] = a.unsafePerformIO
    maybeA match {
      case Some(ax)  => Ok(ax.asJson)
      case None => NotFound()
    }
  }

  def toScalatagsHtml[A](a: Handler[A])(implicit A: EntityEncoder[A]): Task[Response] = {
    Ok(a.unsafePerformIO)
  }
}
