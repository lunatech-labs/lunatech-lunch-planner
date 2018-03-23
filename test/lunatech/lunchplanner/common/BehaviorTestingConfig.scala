package lunatech.lunchplanner.common

import org.scalatest.{ BeforeAndAfterAll, MustMatchers }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import slick.jdbc.JdbcBackend

trait BehaviorTestingConfig
  extends PlaySpec
    with MockitoSugar
    with BeforeAndAfterAll
    with MustMatchers
    with ScalaFutures
    with DBConnectionProvider
    with TestDatabaseProvider {

  implicit val dbConnection: DBConnection = DBConnectionProvider.dbConnection
  implicit val jdbc: JdbcBackend#DatabaseDef = DBConnectionProvider.jdbc
}
