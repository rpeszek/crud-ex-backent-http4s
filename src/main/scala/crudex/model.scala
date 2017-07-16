package crudex

import io.circe.Encoder

import scalaz._
import Scalaz._
import crudex.app.Common.{Entity, IntId}

/**
  */
object model {

  case class ThingId(id:Int)
  case class Thing(name:String, description:String, userId: Option[UserId])
  type ThingEntity = Entity[ThingId, Thing]

  case class UserId(id:Int)
  case class User(userFirstName:String, userLastName: String)
  type UserEntity = Entity[UserId, User]

  /*
    TODO match Haskell version and do not use List
    data LoggerLevel = Info | Std| Crit
    data LoggerFlag = LApp | LIn | LOut | LUpdate | LView | ...
  */
  case class ElmLoggerConfig(logLevel: String, logFlags: List[String] )
  case class ElmConfig(elmProgName: String, logConfig: ElmLoggerConfig, layout: String)

  object instances {
    import io.circe._
    import io.circe.literal._

    implicit val thidIdFromInt: IntId[ThingId] = new IntId[ThingId] {
      override def fromInt: (Int) => ThingId = i => ThingId(i)
    }
    implicit val thingIdEncoder: Encoder[ThingId] =
      Encoder.encodeLong.contramap[ThingId](_.id)
    implicit val userIdEncoder: Encoder[UserId] =
      Encoder.encodeLong.contramap[UserId](_.id)

    //explicit use of (ev) resolves implicit ambiguity
    implicit def thinIListEncoder[A](implicit ev: Encoder[A]): Encoder[IList[A]] =
      Encoder.encodeList(ev).contramap[IList[A]](_.toList)

  }
}
