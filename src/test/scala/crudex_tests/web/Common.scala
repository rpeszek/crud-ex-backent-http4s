package crudex_tests.web

import crudex.model.{UserId, ThingId}
import io.circe.Decoder

import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

import scalaz.concurrent.Task
import org.specs2.matcher.Matcher
import org.specs2.matcher.MatchersImplicits._
/**
  */
object Common {

  //TODO cannot find Task matchers in scalaz spec2. scalaz-concurrent-spec2?
  def returnTheSameValue[A]: Matcher[Task[Tuple2[A,A]]] = { pair: Task[Tuple2[A, A]] => {
       val unwrapped = pair.unsafePerformSync
       (unwrapped._1 == unwrapped._2, s"Not matching ${unwrapped._1} and ${unwrapped._2}")
     }
  }

  def returnValueOf[A](res: A): Matcher[Task[A]] = { task: Task[A] =>
    (res == task.unsafePerformSync, s"did not match ${res}")
  }

  object instances {
    implicit def evHttp4sFromJsonDecoder[A](implicit ev: Decoder[A]): EntityDecoder[A] = jsonOf[A]

    implicit val evUserIdDecoder: Decoder[UserId] = Decoder.decodeInt.map(UserId.apply)
    implicit val evThingIdDecoder: Decoder[ThingId] = Decoder.decodeInt.map(ThingId.apply)
  }
}
