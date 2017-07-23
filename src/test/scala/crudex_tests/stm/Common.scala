package crudex_tests.stm

import scalaz._, Scalaz._
import crudex.utils.StmFree._

import scalaz.concurrent.Task

/**
  * Some copied from https://github.com/tpolecat/examples/blob/master/src/main/scala/eg/StmSanta.scala
  */
object Common {
  type ThreadId = Long

  def check(b: => Boolean): STM[Unit] =
    b ? ().point[STM] | retry

  def forkIO(a: Task[Unit]): Task[ThreadId] =
    Task {
      val t = new Thread {
        setDaemon(true)
        override def run: Unit = a.unsafePerformSync
      }
      t.start()
      t.getId()
    }

  def forever(act: Task[Unit]): Task[Unit] =
    act.flatMap(_ => forever(act)) // N.B. important to never yield, otherwise we leak heap

}
