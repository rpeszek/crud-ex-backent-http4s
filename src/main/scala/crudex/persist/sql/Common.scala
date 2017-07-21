package crudex.persist.sql

import scalaz._
import Scalaz._
import doobie.imports._
import doobie.h2.imports._

import scalaz.concurrent.Task


object Common {
  //type TransactionalSqlEff[A] = Task[A]
  type TransactionalSqlEff[A] = ReaderT[Task, Transactor[Task], A]

  lazy val xaTask : Task[Transactor[Task]] = {
    H2Transactor[Task]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")
  }

  def runTransaction[A]: ConnectionIO[A] => TransactionalSqlEff[A] =  a =>
    ReaderT { xa: Transactor[Task] =>
       a.transact[Task](xa)
    }

  def createTables: Task[Unit] =
    xaTask >>= { xa =>
      runTransaction(
        for {
          _ <- sql"CREATE SEQUENCE ID_SEQ;".update.run

          _ <-
          sql"""
             CREATE TABLE THING (
             id   integer PRIMARY KEY,
             name varchar(255) NOT NULL,
             description varchar(255),
             user_id integer
            );
            """.update.run
        } yield (())
      ).run(xa)
    }

  object instances {
      implicit def evSqlToTaskNt: TransactionalSqlEff ~> Task = new (TransactionalSqlEff ~> Task) {
        def apply[A](a: TransactionalSqlEff[A]): Task[A] =
          for {
            xa <- xaTask
            result <- a.run(xa)
          } yield (result)
      }
  }
}
