package lunatech.lunchplanner.common

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfter, BeforeAndAfterAll, MustMatchers }
import org.scalatestplus.play.{ OneServerPerSuite, PlaySpec }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.jdbc.JdbcBackend

trait AcceptanceSpec extends PlaySpec
  with MustMatchers
  with ScalaFutures
  with BeforeAndAfterAll
  with OneServerPerSuite
  with DBConnectionProvider {

  override def db: JdbcBackend#DatabaseDef = app.injector.instanceOf(classOf[DatabaseConfigProvider]).get[JdbcProfile].db
}
