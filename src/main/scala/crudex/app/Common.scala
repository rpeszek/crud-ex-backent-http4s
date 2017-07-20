package crudex.app

import scalaz._
import Scalaz._
import scalaz.concurrent.Task
import scalaz.effect.IO
/**
  *
  */
object Common {

  //region Common Type classes abstract over CRUD entity and persistence effects

  /*  Legend: K=key/id, D=record under that id, E=effect */

  case class Entity[K,D](id: K, entity: D)

  trait IntId[K] {
    def fromInt: Int => K
  }

  /* Currently app used retrieveAll and retrieveRecord only */
  trait PersistRead[K,D,E[_]] {
     def retrieveAll: E[IList[Entity[K,D]]]

     def retrieveEntity(id: K)(implicit E:Monad[E]): E[Option[Entity[K,D]]] =
        E.map(retrieveRecord(id))(_.map(Entity(id,_)))

     def retrieveRecord(id: K)(implicit E:Monad[E]): E[Option[D]]  =
        E.map(retrieveEntity(id))((maybeEntity: Option[Entity[K,D]]) => maybeEntity.map(_.entity))
  }

  trait PersistCrud[K,D, E[_]] extends PersistRead[K,D,E] {
     def create: D => E[Entity[K,D]]
     def update: K => D => E[Option[D]]  //E[Option[V]] to reflect use of invalid K - key
     def delete: K => E[Unit]
  }

  /*
    In addition, natural transformation (~>) defined in scalaz is used to convert DbEffect types to Tasks
   */

  //endregion


  object instances {

    //Natural Transformation is used to convert DbEffect types to Tasks
    //this app uses Task itself as DbEffect for Sql effects
    implicit def evTaskIsTask: Task ~> Task = new (Task ~> Task) {
      def apply[A](fa: Task[A]): Task[A] = fa
    }
    //IO no longer used as Db Effect no need to prove correspondence between IO and Task
  }

}
