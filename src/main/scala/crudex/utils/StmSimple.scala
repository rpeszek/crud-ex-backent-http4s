package crudex.utils

import scalaz.effect.IO
import scalaz._
import Scalaz._
import scala.concurrent.stm.{retry => stmRetry, _}
import scalaz.concurrent.Task

/*
  Currently not used, replaced with FreeStm

  Adjusted from https://gist.github.com/tpolecat/5672105
  StmFree is more developed and semantically more correct.
  This version implements only a limited set of STM operations
  Uses a hack defining STM over IO monad.
 */
object StmSimple {
  type STM[A] = ReaderT[IO, InTxn, A]
  type TVar[A] = Ref[A]

  def readTVar[A](r: TVar[A]): STM[A] =
    Reader { implicit t: InTxn => r() }.lift[IO]

  def putTVar[A](r: TVar[A], a: A): STM[A] =
    Reader { implicit t: InTxn => {r() = a; a}}.lift[IO]

  def newTVarIO[A](a: A): IO[TVar[A]] =
    IO(Ref(a))

  def newTVarTask[A](a: A): Task[TVar[A]] =
    Task(Ref(a))

  def atomically[A](a: STM[A]): IO[A] = {
    //atomic(a.run)
    val unwrapped = atomic { implicit txn =>
       a.run(txn).unsafePerformIO
    }
    IO(unwrapped)
  }

  def atomicallyAsTask[A](a: STM[A]): Task[A] = {
    val unwrapped = atomic { implicit txn =>
      a.run(txn).unsafePerformIO
    }
    Task(unwrapped)
  }

  /*
   Mimics STMContainers-Map in Haskell
   (this object could be moved out)
   */
  object StmContainers {
    case class StmMap[K,V] private (innerMap: TMap[K,V]) {
      /* insert :: Key k => v -> k -> Map k v -> STM () */
      def insert(pair: (K, V)): STM[Unit] = {
        Reader { implicit t: InTxn => {
          this.innerMap.put(pair._1, pair._2);
          ()
        }
        }.lift[IO]
      }
      /* delete :: Key k => k -> Map k v -> STM () */
      def delete(k:K): STM[Unit] = {
        Reader { implicit t: InTxn => {
          this.innerMap.remove(k);
          ()
        }
        }.lift[IO]
      }
      /* lookup :: Key k => k -> Map k v -> STM (Maybe v) */
      def get(k:K): STM[Option[V]] = {
        Reader { implicit t: InTxn =>
          this.innerMap.get(k);
        }.lift[IO]
      }
      /* null :: Map k v -> STM Bool */
      def isEmpty(): STM[Boolean] = {
        Reader { implicit t: InTxn =>
          this.innerMap.isEmpty;
        }.lift[IO]
      }
      /* size :: Map k v -> STM Int */
      def size: STM[Int] = {
        Reader { implicit t: InTxn => innerMap.size }.lift[IO]
      }

      def toList:STM[IList[(K,V)]] = {
        Reader { implicit t: InTxn => {
          val map = innerMap.snapshot
          IList.fromList(map.toList)
        }}.lift[IO]
      }
    }

    //defining newMap as IO[StmMap[K,V]] using IO(TMap.empty[K,V]) did not work, the changes did not stick
    def newMap[K,V] : STM[StmMap[K,V]] = StmMap(TMap.empty[K,V]).pure[STM]
  }

}


