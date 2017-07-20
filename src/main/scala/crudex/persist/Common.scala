package crudex.persist


import scalaz.concurrent.Task

/**
  */
object Common {
   type DefaultPersistEff[A] = Task[A]

   //DbEffectToTask instance already implemented for Task in crudex.app.Common.instances
}
