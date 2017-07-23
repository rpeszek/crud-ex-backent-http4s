package crudex.utils

/*
 Modified version of, updated to Scalaz 7.2
 https://github.com/tpolecat/examples/blob/master/src/main/scala/eg/FreeSTM.scala
  */
import scalaz.effect.IO
import scalaz.effect.IO._
import scalaz._
import Scalaz._
import scalaz.concurrent.Task

/**
  * Pure functional STM using a free monad to hide the underlying Scala STM. Refs and transaction
  * tokens are not observable. The end result looks a lot like Haskell STM.
  * Includes StmMap implementation that mimics Haskell STM containers library.
  */
object StmFree {
  import scalaz.Free._
  import scala.concurrent.stm.{retry => stmRetry, _}

  //region API Types

  /* Transactional mutable variable type */
  final class TVar[A](private[StmFree] val ref: Ref[A])
  /* STM effect Type */
  type STM[A]  = Free[Op, A]

  //TODO is this needed or somehow using Free makes it redundant?
  implicit val MonadSTM: Monad[STM] = Free.freeMonad[Op]

  //endregion

  //region Boilerplate - Algebra of STM operations

  // Algebra of STM Operations (private)
  sealed trait Op[A]
  private object Op {
    case class  NewTVar[A](a: A) extends Op[TVar[A]]
    case class  ReadTVar[A](fa: TVar[A]) extends Op[A]
    case class  WriteTVar[A](fa: TVar[A], a: A) extends Op[Unit]
    case object Retry extends Op[Unit]
    case class  Delay[A](fa: () => A) extends Op[A]
    case class  Process[A](fa: InTxn => A) extends Op[A]
  }
  import Op._

  //endregion

  //region Boilerplate - Interpreter

  // Interpret Op to Reader
  private type InTxnReader[A] = InTxn => A
  private val interpOp: Op ~> InTxnReader =
    new (Op ~> InTxnReader) {
      def apply[A](fa: Op[A]): InTxnReader[A] =
        fa match {
          case ReadTVar(fa) => { implicit tx => fa.ref() }
          case WriteTVar(fa, a) => { implicit tx => fa.ref() = a }
          case Retry            => { implicit tx => stmRetry }
          case NewTVar(a)       => { implicit tx => new TVar(Ref(a)) }
          case Delay(fa)        => { implicit tx => fa() }
          case Process(fa)      => { implicit tx => fa(tx) }
        }
    }

  // Interpret STM to Reader
  private def interpF[A](a: STM[A]): InTxnReader[A] =
    a.foldMap(interpOp)

  //private operation used in defining StmContainers
  private def process[A]:(InTxn => A) => STM[A] = fa => liftF(Process(fa))

  //endregion

  //region STM API methods

  def newTVar[A](a: A): STM[TVar[A]] = liftF[Op, TVar[A]](NewTVar(a))
  def readTVar[A](r: TVar[A]): STM[A] = liftF(ReadTVar(r))
  def putTVar[A](r: TVar[A], a: A): STM[Unit] = liftF[Op, Unit](WriteTVar(r, a))
  val retry: STM[Unit] = liftF(Retry)
  def fail[A](e: Exception): STM[A] = process[A]{ t: InTxn => throw e}

  //orElse combinator (not tested)
  def orElse[A](a: STM[A], b: STM[A]): STM[A] =
    liftF(Delay(() => atomic(interpF(a)).orAtomic(interpF(b))))
  def atomically[A](a: STM[A]): IO[A] =
    atomic(interpF(a)).pure[IO] //just wrapping in IO does not work, TVar looses its value
  def atomicallyAsTask[A](a: STM[A]): Task[A] =
    atomic(interpF(a)).pure[Task]

  def newTVarIO[A](a: A): IO[TVar[A]] = atomically(newTVar(a))
  def newTVarTask[A](a: A): Task[TVar[A]] = atomicallyAsTask(newTVar(a))

  //endregion

  //region StmContainers library

  /*
   Mimics STMContainers-Map in Haskell
   Currently implements transactional map (StmMap) only.
  */
  object StmContainers {
    /* Transactionally mutable map */
    case class StmMap[K, V] private(innerMap: TMap[K, V]) {
      /* insert :: Key k => v -> k -> Map k v -> STM () */
      def insert(pair: (K, V)): STM[Unit] =
        process { t: InTxn => {
          implicit val tx: InTxn = t
          this.innerMap.put(pair._1, pair._2);
          ()
        }
       }
      /* delete :: Key k => k -> Map k v -> STM () */
      def delete(k: K): STM[Unit] =
        process { t: InTxn => {
          implicit val tx: InTxn = t
          this.innerMap.remove(k);
          ()
        }
       }
      /* lookup :: Key k => k -> Map k v -> STM (Maybe v) */
      def get(k: K): STM[Option[V]] =
        process { t: InTxn =>
          implicit val tx: InTxn = t
          this.innerMap.get(k);
        }
      /* null :: Map k v -> STM Bool */
      def isEmpty(): STM[Boolean] =
        process { t: InTxn =>
          implicit val tx: InTxn = t
          this.innerMap.isEmpty;
        }
      /* size :: Map k v -> STM Int */
      def size: STM[Int] =
        process { t: InTxn =>
          implicit val tx: InTxn = t
          innerMap.size
        }
      def toList: STM[IList[(K, V)]] =
        process { t: InTxn => {
          implicit val tx: InTxn = t
          val map = innerMap.snapshot
          IList.fromList(map.toList)
         }
        }
    }

    //defining newMap as IO[StmMap[K,V]] using IO(TMap.empty[K,V]) did not work, the changes did not stick
    def newMap[K, V]: STM[StmMap[K, V]] = MonadSTM.pure(StmMap(TMap.empty[K, V]))
  }

  //endregion
}

