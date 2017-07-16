package crudex.app

import scalaz._
import Scalaz._
import scalaz.effect.IO
/**
  *
  */
object Common {
   //TODO, currently not used
  sealed trait AppConfig

  //region Common Type classes abstract over CRUD entity and persistence effect used

  /*  Legend: K=key/id, D=record under that id, E=effect */

  case class Entity[K,D](id: K, entity: D)

  trait IntId[K] {
    def fromInt: Int => K
  }

  /* Currently app used retrieveAll and retrieveRecord only */
  trait ReadableEntity[K,D,E[_]] {
     def retrieveAll: E[IList[Entity[K,D]]]

     def retrieveEntity(id: K)(implicit E:Monad[E]): E[Option[Entity[K,D]]] =
        E.map(retrieveRecord(id))(_.map(Entity(id,_)))

     def retrieveRecord(id: K)(implicit E:Monad[E]): E[Option[D]]  =
        E.map(retrieveEntity(id))((maybeEntity: Option[Entity[K,D]]) => maybeEntity.map(_.entity))
  }

  trait EditableEntity[K,D, E[_]] extends ReadableEntity[K,D,E] {
     def create: D => E[Entity[K,D]]
     def update: K => D => E[D]  //TODO should this be E[Option[V]], currently this acts as create if not found
     def delete: K => E[Unit]
  }

  /*
    This abstraction allows handling arbitrary persistence effects.
    Treats persistent effect as T-Algebra
   */
  //TODO This runs effects early, think how to create http4s services so that the effect can be resolved in the Main class
  trait RunPersistence[E[_]] {
    def runPersistEffect[A](a: E[A]): A
  }

  //endregion

  object instances {
    implicit def ioAsPersistHandler: RunPersistence[IO] = new RunPersistence[IO] {
      override def runPersistEffect[A](a: IO[A]): A = a.unsafePerformIO
    }
  }

}
