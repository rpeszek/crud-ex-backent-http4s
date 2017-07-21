package sql

/**
   */

import crudex.persist.sql.ThingSql._
import crudex.persist.sql.Common._
import crudex.model._
import doobie.h2.imports.H2Transactor
import doobie.util.iolite.IOLite
import doobie.specs2.imports._
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scalaz.concurrent.Task


trait dbSetup extends Scope {
  val tab = createTables.unsafePerformSync
}
object AnalysisTestSpec extends Specification with AnalysisSpec {

  //val transactor = TestMe2.getTransactor
    val transactor /*: Transactor[IOLite] */ = {
     H2Transactor[IOLite]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "").unsafePerformIO
    }
  "this is the first example" in new dbSetup {
     check(getThingsSql)
     check(getThingSql(ThingId(0)))
  }
}