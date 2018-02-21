package lunatech.lunchplanner.common

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, MustMatchers }
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.PlaySpec
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcBackend

trait AcceptanceSpec extends PlaySpec
  with MustMatchers
  with ScalaFutures
  with BeforeAndAfterAll
  with GuiceOneServerPerSuite
  with DBConnectionProvider {

  override def db: JdbcBackend#DatabaseDef = app.injector.instanceOf(classOf[DatabaseConfigProvider]).get[JdbcProfile].db
}
