package lunatech.lunchplanner.common

import org.scalatestplus.play.guice.GuiceFakeApplicationFactory
import slick.jdbc.JdbcBackend

trait DBConnectionProvider {
  def jdbc: JdbcBackend#DatabaseDef
}

object DBConnectionProvider extends GuiceFakeApplicationFactory {
  val app = fakeApplication

  val dbConnection: DBConnection = app.injector.instanceOf[DBConnection]

  val jdbc: JdbcBackend#DatabaseDef = dbConnection.db
}
