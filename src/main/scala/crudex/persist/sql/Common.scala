package crudex.persist.sql

import crudex.app.Common.RunDbEffect

import scalaz._
import Scalaz._
import doobie.imports._
import doobie.h2.imports._


object Common {
  type TransactionalSqlEff[A] = IOLite[A]

  object instances {
    implicit def sqlAsDbEffect: RunDbEffect[TransactionalSqlEff] = new RunDbEffect[TransactionalSqlEff] {
      override def runDbEffect[A](a: TransactionalSqlEff[A]): A = {
        a.unsafePerformIO
      }
    }
  }

  lazy val xaIO : IOLite[H2Transactor[IOLite]] = {
    H2Transactor[IOLite]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")
  }

  def runTransaction[A]: ConnectionIO[A] => TransactionalSqlEff[A] = a =>
    xaIO >>= a.transact[IOLite]

  def createTables: TransactionalSqlEff[Int] =
    runTransaction(
      sql"""
         CREATE TABLE THING (
         id   integer auto_increment,
         name varchar(255) NOT NULL,
         description varchar(255),
         user_id integer
    )
      """.update.run
    )
}
