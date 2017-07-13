package crudex.stm

import scalaz.effect.IO
import scalaz._
import Scalaz._
import crudex.utils.StmMonad._
import crudex.utils.STMContainers._
import crudex.model._

/**
  */
object ThingStm {

   type Handler[A] = IO[A]
   type ThingStore = StmMap[ThingId, Thing]

   def initThingStore: Handler[ThingStore] =
         atomically(
           for {
             store <- newMap[ThingId, Thing]
             _     <- store.insert(ThingId(0), Thing("testName1", "testDesc1", None))
             _     <- store.insert(ThingId(1), Thing("testName2", "testDesc2", None))
           }yield(store)
         )


   private def pairToEntityIso: Tuple2[ThingId, Thing] => ThingEntity = (pair: Tuple2[ThingId, Thing]) => new Entity(pair._1, pair._2)

   def getThings: ThingStore => Handler[IList[ThingEntity]] = store =>
        atomically(
           store.toList.map((list) => list.map(pairToEntityIso))
        )

  def getThing:  ThingId => ThingStore => Handler[Option[Thing]] =  thingId => store =>
    atomically( store.get(thingId) )

  //  postThingH
   def createThing :  Thing => ThingStore => Handler[ThingEntity] =  thing => store =>
       atomically(
         for {
           count <- store.size
           id = ThingId(count)
           _     <- store.insert((id, thing))
         } yield (Entity(id, thing))
       )


  //TODO this probably does not work may need to fix insert to remove if exists
   def modifyThing:  ThingId => Thing => ThingStore => Handler[Thing] = thingId => thing => store =>
        atomically(
          for {
            _ <- store.insert((thingId, thing))
          } yield(thing)
        )

   def deleteThing: ThingId => ThingStore => Handler[Unit] =  thingId => store =>
        atomically(
           store.delete(thingId)
        )

}
