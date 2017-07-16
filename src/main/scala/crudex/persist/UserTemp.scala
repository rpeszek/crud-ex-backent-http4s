package crudex.persist

import crudex.app.Common.Entity
import crudex.model.{User, UserEntity, UserId}
import crudex.persist.Common.DefaultPersistEff

import scalaz._
import Scalaz._

/**
  * Temp because it was not STM-ed yet
  */
object UserTemp {

  val tempUsers : IList[UserEntity] = IList(
     Entity(UserId(1), User("Alonzo", "Church")),
     Entity(UserId(2), User("Alan", "Turing"))
  )

  def getUsers: DefaultPersistEff[IList[UserEntity]] =
     tempUsers.pure[DefaultPersistEff]
}
