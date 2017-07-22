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
  */
object ThingSql {



  /*
     Query/Update methods are right next to actual method returning ConnectIO.
     The split betten xyzSql methods returning Query or Update types allows
     spec2 tests of test.sql correctness
  */

  //TODO IList not typechecking directly
  //implicit def IListMeta[A: TypeTag](implicit ev: Meta[List[A]]): Meta[IList[A]] =
  //  ev.nxmap[IList[A]](IList.fromList, _.toList)
  def getThingsSql: Query0[ThingEntity] =
    sql"""
          SELECT id, name, description, user_id
          FROM THING
      """.query[ThingEntity]
  def getThings: ConnectionIO[IList[ThingEntity]] =
     getThingsSql.list.map(IList.fromList)


  def getThingSql (thingId: ThingId): Query0[Thing] =
    sql"SELECT name, description, user_id FROM THING WHERE id=${thingId.id}".query[Thing]
  def getThing:  ThingId => ConnectionIO[Option[Thing]] = thingId =>
      getThingSql(thingId).option


  def nextId : Query0[Int] = sql"SELECT ID_SEQ.nextval".query[Int]
  def createThingSql: ThingId => Thing => Update0 = thingId => thing =>
    sql"INSERT INTO THING(id, name,description,user_id) VALUES (${thingId.id}, ${thing.name}, ${thing.description}, ${thing.userId})".update
  def createThing :  Thing => ConnectionIO[ThingEntity] = thing =>
     for {
        id <- nextId.unique
        _ <-  createThingSql(ThingId(id))(thing).run
        //TODO fail if not inserted
        //TODO re-retrieving thing would be better
     } yield(Entity(ThingId(id), thing))


  def modifyThingSql: ThingId => Thing => Update0 = thingId => thing =>
    sql"""UPDATE THING
            SET name = ${thing.name}, description = ${thing.description}, user_id = ${thing.userId}
            WHERE id = ${thingId.id}
       """.update
  def modifyThing:  ThingId => Thing => ConnectionIO[Option[Thing]] = thingId => thing =>
     for {
       i <- modifyThingSql(thingId)(thing).run
       //TODO re-retrieving thing would be better
     } yield(if(i===1) Some(thing) else None)


  def deleteThingSql: ThingId =>  Update0 = thingId =>
    sql"DELETE FROM THING WHERE id=${thingId.id}".update
  def deleteThing: ThingId =>  ConnectionIO[Unit] = thingId =>
    for {
       _ <- deleteThingSql(thingId).run
       //TODO do something if nothing was deleted
    }yield(())

  object instances {

    implicit def evThingCrudWithSql: PersistCrud[ThingId, Thing, TransactionalSqlEff] = new PersistCrud[ThingId, Thing, TransactionalSqlEff]{
      override def retrieveAll: TransactionalSqlEff[IList[Entity[ThingId, Thing]]] = runTransaction(getThings)
      override def retrieveRecord(id: ThingId)(implicit E: Monad[TransactionalSqlEff]): TransactionalSqlEff[Option[Thing]] = runTransaction(getThing(id))
      override def create: (Thing) => TransactionalSqlEff[Entity[ThingId, Thing]] = thing => runTransaction(createThing(thing))
      override def update: (ThingId) => (Thing) => TransactionalSqlEff[Option[Thing]] = thingId => thing => runTransaction(modifyThing(thingId)(thing))
      override def delete: (ThingId) => TransactionalSqlEff[Unit] = thingId => runTransaction(deleteThing(thingId))
    }
  }

}
