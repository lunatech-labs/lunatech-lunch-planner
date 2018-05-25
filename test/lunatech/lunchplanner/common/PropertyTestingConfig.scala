package lunatech.lunchplanner.common

import org.scalatest.{ BeforeAndAfterAll, MustMatchers, Suite, SuiteMixin }
import slick.jdbc.JdbcBackend

trait PropertyTestingConfig
  extends DBConnectionProvider
    with TestDatabaseProvider
    with Suite
    with SuiteMixin
    with BeforeAndAfterAll {

  implicit val dbConnection: DBConnection = DBConnectionProvider.dbConnection

  implicit val jdbc: JdbcBackend#DatabaseDef = DBConnectionProvider.jdbc
}
