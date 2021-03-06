package crudex.persist.stm

import scalaz.effect.IO
import scalaz._
import Scalaz._
import scalaz.effect.IO
import crudex.utils.StmFree._
import crudex.utils.StmFree.StmContainers._
import crudex.model._
import crudex.app.Common._

import scalaz.concurrent.Task

/**
  */
object ThingStm {

  /*
   Type definitions.
   This design mimics Sql in which ther is inner effect type ConnectionIO (here ThingStmEff) and applying transactions
    results in outer effect IOLite (here ThingAtomicStmEff).
  */
   type ThingStore = StmMap[ThingId, Thing]
   type ThingAtomicStmEff[A] = ReaderT[Task, ThingStore, A]
   type ThingStmEff[A] = ReaderT[STM, ThingStore, A]


   /* methods */
   def initThingStore: Task[ThingStore] =
         atomicallyAsTask(
           for {
             store <- newMap[ThingId, Thing]
             _     <- store.insert(ThingId(0), Thing("testName1", "testDesc1", None))
             _     <- store.insert(ThingId(1), Thing("testName2", "testDesc2", None))
           }yield(store)
         )


   private def pairToEntityIso: Tuple2[ThingId, Thing] => ThingEntity = (pair: Tuple2[ThingId, Thing]) => new Entity(pair._1, pair._2)

   def runAtomically[A]: ThingStmEff[A] => ThingAtomicStmEff[A] = a =>
     ReaderT { store: ThingStore =>
       atomicallyAsTask(
         a.run(store)
       )
     }


   def getThings: ThingStmEff[IList[ThingEntity]] =
     ReaderT { store: ThingStore => {
         store.toList.map((list) => list.map(pairToEntityIso))
       }: STM[IList[ThingEntity]]   //scala typechecker needs help
     }

   def getThing:  ThingId => ThingStmEff[Option[Thing]] = thingId =>
     ReaderT { store: ThingStore =>
       store.get(thingId)
     }

   def createThing :  Thing => ThingStmEff[ThingEntity] = thing =>
     ReaderT { store: ThingStore => {
          for {
             count <- store.size
             id = ThingId(count)
             _ <- store.insert((id, thing))
           } yield (Entity(id, thing))
         } : STM[ThingEntity]    //scala typechecker needs help
      }

   def modifyThing:  ThingId => Thing => ThingStmEff[Option[Thing]] = thingId => thing =>
     ReaderT { store: ThingStore => {
        for {
          existing <- store.get(thingId)
          _ <- if (existing.isDefined) store.insert((thingId, thing)) else Unit.pure[STM]
        } yield (if (existing.isDefined) Some(thing) else None)
      }: STM[Option[Thing]] //scala typechecker needs help
     }

   def deleteThing: ThingId =>  ThingStmEff[Option[Unit]] = thingId =>
     ReaderT { store: ThingStore => {
       for {
         existing <- store.get(thingId)
         _ <-  store.delete(thingId)
       } yield (if (existing.isDefined) Some(()) else None)
      } : STM[Option[Unit]] //scala typechecker needs help
     }


  object instances {
    val appConfig = initThingStore

    implicit def evStmToTaskNt: ThingAtomicStmEff ~> Task = new (ThingAtomicStmEff ~> Task) {
      def apply[A](a: ThingAtomicStmEff[A]): Task[A] =
        for {
          store  <- appConfig
          result <- a.run(store)
        } yield (result)
    }

    implicit def evThingCrudWithStm: PersistCrud[ThingId, Thing, ThingAtomicStmEff] = new PersistCrud[ThingId, Thing, ThingAtomicStmEff]{
      override def retrieveAll: ThingAtomicStmEff[IList[Entity[ThingId, Thing]]] = runAtomically(getThings)
      override def retrieveRecord(id: ThingId)(implicit E: Monad[ThingAtomicStmEff]): ThingAtomicStmEff[Option[Thing]] = runAtomically(getThing(id))
      override def create: (Thing) => ThingAtomicStmEff[Entity[ThingId, Thing]] = thing => runAtomically(createThing(thing))
      override def update: (ThingId) => (Thing) => ThingAtomicStmEff[Option[Thing]] =  thingId => thing => runAtomically(modifyThing(thingId)(thing))
      override def delete: (ThingId) => ThingAtomicStmEff[Option[Unit]] = thingId => runAtomically(deleteThing(thingId))
    }
  }
}
