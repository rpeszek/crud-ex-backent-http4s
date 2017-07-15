package crudex.stm

import crudex.model.{Entity, UserEntity, UserId, User}
import crudex.utils.Common.Handler

import scalaz._
import Scalaz._
import scalaz.effect.IO

/**
  * Temp because it was not STM-ed yet
  */
object UserTemp {

  val tempUsers : IList[UserEntity] = IList(
     Entity(UserId(1), User("Alonzo", "Church")),
     Entity(UserId(2), User("Alan", "Turing"))
  )

  def getUsers: Handler[IList[UserEntity]] =
     tempUsers.pure[IO]
}
