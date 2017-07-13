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

  object instances {
    implicit val thingIdEncoder: Encoder[ThingId] =
      Encoder.encodeLong.contramap[ThingId](_.id)

//TODO getting ambigous implicit values
//      implicit def thinIListEncoder[A](implicit A: Encoder[A], ev: Encoder[List[A]]): Encoder[IList[A]] =
//        Encoder.instance { iList: IList[A] =>
//           json"""${Encoder.encodeList.asJson(iList.toList)}"""
//        }


//TODO getting ambigous implicit values
//      implicit val thinListEncoder: Encoder[IList[ThingEntity]] =
//        Encoder.encodeList.contramap[IList[ThingEntity]](???)

  }
}
