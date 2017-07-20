package crudex.persist.sql

import scalaz._
import Scalaz._
import doobie.imports._
import doobie.h2.imports._

import scalaz.concurrent.Task


object Common {
  type TransactionalSqlEff[A] = Task[A]

  //object instances {
  //  evTaskToTask is already defined in crudex.app.Common, no need to prove TransactionalSqlEff ~> Task
  //}

  lazy val xaIO : Task[H2Transactor[Task]] = {
    H2Transactor[Task]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")
  }

  def runTransaction[A]: ConnectionIO[A] => TransactionalSqlEff[A] = a =>
    xaIO >>= a.transact[Task]

  def createTables: TransactionalSqlEff[Unit] =
    runTransaction(
      for {
        _ <- sql"CREATE SEQUENCE ID_SEQ;".update.run

        _ <- sql"""
             CREATE TABLE THING (
             id   integer PRIMARY KEY,
             name varchar(255) NOT NULL,
             description varchar(255),
             user_id integer
            );
            """.update.run
      } yield(())
    )
}
