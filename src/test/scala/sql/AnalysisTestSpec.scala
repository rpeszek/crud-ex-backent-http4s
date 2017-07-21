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


trait dbSetup extends Scope {
  val tab = createTables.unsafePerformSync
}
object AnalysisTestSpec extends Specification with AnalysisSpec {

  val transactor /*: Transactor[IOLite] */ = {
     H2Transactor[IOLite]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "").unsafePerformIO
  }

  "check sql definitions for Thing entity" in new dbSetup {
     check(getThingsSql)
     check(getThingSql(ThingId(0)))
     check(nextId)
     check(createThingSql(ThingId(-1))(Thing("testNm","desc",None)))
     check(modifyThingSql(ThingId(-1))(Thing("testNm","desc",None)))
     check(deleteThingSql(ThingId(-1)))
  }
}