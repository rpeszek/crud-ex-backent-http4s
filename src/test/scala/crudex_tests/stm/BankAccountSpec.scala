package crudex_tests.stm


import crudex.utils.StmFree._
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Prop, Properties}

import scalaz._
import Scalaz._
import scalaz.concurrent.Task

/**
  */
object BankAccountSpec extends Properties("BankAccount") {

  object banking {
    type Dollar = Int
    case class BankAccount(name: String, amount: Dollar)
    case class OverdraftExeption(msg: String) extends Exception(msg)

    def adjustAccount: TVar[BankAccount] => Dollar => STM[Unit]= accTvar => dollars =>
       for {
         acc <- readTVar(accTvar)
         res <- if(acc.amount + dollars < 0) fail(OverdraftExeption(s"${acc} does not have ${- dollars}")) else putTVar(accTvar, acc.copy(amount = acc.amount + dollars))
       } yield (res)

    def transferMoney: TVar[BankAccount] => TVar[BankAccount] => Dollar => Task[Unit] = accTvar1 => accTvar2 => dollars =>
        atomicallyAsTask(
           for{
              _ <- adjustAccount(accTvar1)(dollars)
              _ <- adjustAccount(accTvar2)(- dollars)
           } yield(())
        )

    val account1: Task[TVar[BankAccount]] = atomicallyAsTask(newTVar(BankAccount("account1", 1000)))
    val account2: Task[TVar[BankAccount]] = atomicallyAsTask(newTVar(BankAccount("account2", 1000)))
  }

  object aribitrary {
    import org.scalacheck._

    case class TransferDollarAmount(amount: Int)
    implicit lazy val arbDollarAmount: Arbitrary[TransferDollarAmount] = Arbitrary(Gen.choose(-1500,1700).map(TransferDollarAmount.apply))

    case class TransferDollarBundle(list: List[TransferDollarAmount])
    implicit lazy val arbTransBundle: Arbitrary[TransferDollarBundle] = Arbitrary (
      for {
         size <- Gen.choose(1,10)
         list <- Gen.listOfN(size, arbDollarAmount.arbitrary)
      } yield(TransferDollarBundle(list))
    )
  }

  import aribitrary._

  property("bank transfers are transactional") = forAll { transfersBundles: List[TransferDollarBundle]  =>

     val singleTransferTask: TransferDollarAmount => Task[Unit] =  tamount => {
         for {
           acc1 <- banking.account1
           acc2 <- banking.account2
           res <- banking.transferMoney(acc1)(acc2)(tamount.amount).handle {
             case e: banking.OverdraftExeption => Task.now(())
           }
         } yield (res)
       }

    val multiTransferTask: TransferDollarBundle => Task[List[Unit]] = bundle =>
       bundle.list.map(singleTransferTask).sequence


    val tasks = transfersBundles.map(multiTransferTask)

    Task.gatherUnordered(tasks).run

    val totalAfterTransfers = {
       for {
         acc1TVar <- banking.account1
         acc2TVar <- banking.account2
         acc1 <- atomicallyAsTask(readTVar(acc1TVar))
         acc2 <- atomicallyAsTask(readTVar(acc2TVar))
         //_ = println(s"${acc1}, ${acc2}")
       } yield (acc1.amount + acc2.amount)
     }.unsafePerformSync

     totalAfterTransfers == 2000
  }
}
