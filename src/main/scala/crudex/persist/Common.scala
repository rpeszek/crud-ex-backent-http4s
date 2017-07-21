package crudex.persist


import scalaz.concurrent.Task

/**
  */
object Common {
   type DefaultPersistEff[A] = Task[A]
}
