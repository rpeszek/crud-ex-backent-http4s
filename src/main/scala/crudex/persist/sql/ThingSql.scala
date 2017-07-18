package crudex.persist.sql

import shapeless._

import scalaz._
import Scalaz._
import doobie.imports._

import crudex.app.Common.{Entity, PersistCrud}
import crudex.model._
import crudex.persist.sql.Common._

import scala.reflect.runtime.universe.TypeTag

/**
  * Work in progress
  */
object ThingSql {

  //TODO IList not typechecking directly
  //implicit def IListMeta[A: TypeTag](implicit ev: Meta[List[A]]): Meta[IList[A]] =
  //  ev.nxmap[IList[A]](IList.fromList, _.toList)

  def getThings: ConnectionIO[IList[ThingEntity]] = {
    sql"""
          SELECT id, name, description, user_id
          FROM THING
      """.query[ThingEntity].list.map(IList.fromList) /*.query[IList[ThingEntity]]*/

  }

  def getThing:  ThingId => ConnectionIO[Option[Thing]] = thingId =>
    sql"SELECT name, description, user_id FROM THING WHERE id=${thingId.id}".query[Thing].option

  def createThing :  Thing => ConnectionIO[ThingEntity] = thing =>
    for {
      _ <- sql"INSERT INTO THING VALUES (${thing.name}, ${thing.description}, ${thing.userId})".update.run
      id <- sql"SELECT SCOPE_IDENTITY()".query[Int].unique
      _ = println("got " + id)
      //TODO re-retrieving thing would be better
    } yield(Entity(ThingId(id), thing))

  def modifyThing:  ThingId => Thing => ConnectionIO[Option[Thing]] = thingId => thing =>
     for {
       i <- sql"""UPDATE THING
            SET name = ${thing.name}, description = ${thing.description}, user_id = ${thing.userId}
            WHERE id = ${thingId.id}
         """.update.run
       //TODO re-retrieving thing would be better
     } yield(if(i===1) Some(thing) else None)

  def deleteThing: ThingId =>  ConnectionIO[Unit] = thingId =>
    for {
       _ <- sql"DELETE FROM THING WHERE id=${thingId.id}".update.run
       //TODO do something if nothing was deleted
    }yield(())

  object instances {

    implicit def thingCrudDB: PersistCrud[ThingId, Thing, TransactionalSqlEff] = new PersistCrud[ThingId, Thing, TransactionalSqlEff]{
      override def retrieveAll: TransactionalSqlEff[IList[Entity[ThingId, Thing]]] = runTransaction(getThings)
      override def retrieveRecord(id: ThingId)(implicit E: Monad[TransactionalSqlEff]): TransactionalSqlEff[Option[Thing]] = runTransaction(getThing(id))
      override def create: (Thing) => TransactionalSqlEff[Entity[ThingId, Thing]] = thing => runTransaction(createThing(thing))
      override def update: (ThingId) => (Thing) => TransactionalSqlEff[Option[Thing]] = thingId => thing => runTransaction(modifyThing(thingId)(thing))
      override def delete: (ThingId) => TransactionalSqlEff[Unit] = thingId => runTransaction(deleteThing(thingId))
    }
  }

}
