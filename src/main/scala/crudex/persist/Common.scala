package crudex.persist


import scalaz.effect.IO

/**
  */
object Common {
   type DefaultPersistEff[A] = IO[A]

//  IO instance already implemented in crudex.web.Commmon, change this if default changes
//   object instances {
//     implicit def defaultPersistEffHandler: PersistHandler[DefaultPersistEff] = new PersistHandler[DefaultPersistEff] {
//       override def runPersistEffect[A](a: DefaultPersistEff[A]): A = a.unsafePerformIO
//     }
//   }
}
