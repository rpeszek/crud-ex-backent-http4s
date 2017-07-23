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

    implicit val evThingIdFromInt: IntId[ThingId] = new IntId[ThingId] {
      override def fromInt: (Int) => ThingId = i => ThingId(i)
    }
    implicit val evThingIdEqual: Equal[ThingId] = new Equal[ThingId] {
      override def equal(a1: ThingId, a2: ThingId): Boolean = a1.id === a2.id
    }
    implicit val evUserIdEqual: Equal[UserId] = new Equal[UserId] {
      override def equal(a1: UserId, a2: UserId): Boolean = a1.id === a2.id
    }

    implicit val evThingIdEncoder: Encoder[ThingId] =
      Encoder.encodeLong.contramap[ThingId](_.id)
    implicit val evUserIdEncoder: Encoder[UserId] =
      Encoder.encodeLong.contramap[UserId](_.id)

    //explicit use of (ev) resolves implicit ambiguity
    implicit def evIListEncoder[A](implicit ev: Encoder[A]): Encoder[IList[A]] =
      Encoder.encodeList(ev).contramap[IList[A]](_.toList)

  }
}
