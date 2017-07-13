package crudex.stm

import crudex.model.{Entity, UserEntity, UserId, User}

import scalaz._
import Scalaz._
import scalaz.effect.IO

/**
  */
object UserTemp {
  type Handler[A] = IO[A]

  val tempUsers : IList[UserEntity] = IList(
     Entity(UserId(1), User("Alonzo", "Church")),
     Entity(UserId(2), User("Alan", "Turing"))
  )

  def getUsers: Handler[IList[UserEntity]] =
     tempUsers.pure[IO]
}
