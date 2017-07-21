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

  //endregion


}
