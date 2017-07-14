package crudex

import scalaz._
import Scalaz._
import io.circe._
import io.circe.literal._

/**
  */
object model {
  case class Entity[K,V](id: K, entity: V)

  case class ThingId(id:Int)
  case class Thing(name:String, description:String, userId: Option[UserId])
  type ThingEntity = Entity[ThingId, Thing]

  case class UserId(id:Int)
  case class User(userFirstName:String, userLastName: String)
  type UserEntity = Entity[UserId, User]

  //TODO match Haskell version and do not use List
  //data LoggerLevel = Info | Std| Crit
  //data LoggerFlag = LApp | LIn | LOut | LUpdate | LView | ...
  case class ElmLoggerConfig(logLevel: String, logFlags: List[String] )
  case class ElmConfig(elmProgName: String, logConfig: ElmLoggerConfig, layout: String)

  object instances {
    implicit val thingIdEncoder: Encoder[ThingId] =
      Encoder.encodeLong.contramap[ThingId](_.id)
    implicit val userIdEncoder: Encoder[UserId] =
      Encoder.encodeLong.contramap[UserId](_.id)

//TODO getting ambiguous implicit values, figure it out
//      implicit def thinIListEncoder[A](implicit A: Encoder[A], ev: Encoder[List[A]]): Encoder[IList[A]] =
//        Encoder.instance { iList: IList[A] =>
//           json"""${Encoder.encodeList.asJson(iList.toList)}"""
//        }


//TODO getting ambiguous implicit values, figure it out
//      implicit val thinListEncoder: Encoder[IList[ThingEntity]] =
//        Encoder.encodeList.contramap[IList[ThingEntity]](???)

  }
}
