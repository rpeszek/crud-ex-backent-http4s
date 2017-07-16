package crudex.persist.stm

import scalaz.effect.IO
import scalaz._
import Scalaz._
import scalaz.effect.IO

import crudex.utils.StmFree._
import crudex.utils.StmFree.StmContainers._
import crudex.model._
import crudex.app.Common._

/**
  */
object ThingStm {

   /* type definitions */
   type ThingStore = StmMap[ThingId, Thing]
   type StmPersistThingEff[A] = ReaderT[IO, ThingStore, A]

   object instances {
     val appConfig = initThingStore

     implicit def stmPersistEffHandler: RunPersistence[StmPersistThingEff] = new RunPersistence[StmPersistThingEff] {
        override def runPersistEffect[A](a: StmPersistThingEff[A]): A = {
          val r = for {
            store  <- appConfig
            result <- a.run(store)
          } yield (result)

          r.unsafePerformIO
        }
        //^ better than: a.run(appConfig.unsafePerformIO).unsafePerformIO
     }

     implicit def editableStmEntity: EditableEntity[ThingId, Thing, StmPersistThingEff] = new EditableEntity[ThingId, Thing, StmPersistThingEff]{
       override def retrieveAll: StmPersistThingEff[IList[Entity[ThingId, Thing]]] = getThings

       override def retrieveRecord(id: ThingId)(implicit E: Monad[StmPersistThingEff]): StmPersistThingEff[Option[Thing]] = getThing(id)

       override def create: (Thing) => StmPersistThingEff[Entity[ThingId, Thing]] =  createThing

       override def update: (ThingId) => (Thing) => StmPersistThingEff[Thing] =  modifyThing

       override def delete: (ThingId) => StmPersistThingEff[Unit] = deleteThing

     }
   }

   /* methods */
   def initThingStore: IO[ThingStore] =
         atomically(
           for {
             store <- newMap[ThingId, Thing]
             _     <- store.insert(ThingId(0), Thing("testName1", "testDesc1", None))
             _     <- store.insert(ThingId(1), Thing("testName2", "testDesc2", None))
           }yield(store)
         )


   private def pairToEntityIso: Tuple2[ThingId, Thing] => ThingEntity = (pair: Tuple2[ThingId, Thing]) => new Entity(pair._1, pair._2)

   def getThings: StmPersistThingEff[IList[ThingEntity]] =
     ReaderT { store: ThingStore =>
       atomically(
         store.toList.map((list) => list.map(pairToEntityIso))
       )
     }

   def getThing:  ThingId => StmPersistThingEff[Option[Thing]] = thingId =>
     ReaderT { store: ThingStore =>
       atomically(store.get(thingId))
     }

  //  postThingH
   def createThing :  Thing => StmPersistThingEff[ThingEntity] = thing =>
     ReaderT { store: ThingStore =>
       atomically(
         for {
           count <- store.size
           id = ThingId(count)
           _ <- store.insert((id, thing))
         } yield (Entity(id, thing))
       )
     }


   def modifyThing:  ThingId => Thing => StmPersistThingEff[Thing] = thingId => thing =>
     ReaderT { store: ThingStore =>
       atomically(
         for {
           _ <- store.insert((thingId, thing))
         } yield (thing)
       )
     }

   def deleteThing: ThingId =>  StmPersistThingEff[Unit] = thingId =>
     ReaderT { store: ThingStore =>
       atomically(
         store.delete(thingId)
       )
     }

}
