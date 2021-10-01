package lunatech.lunchplanner.common

import com.typesafe.config.ConfigFactory
import lunatech.lunchplanner.persistence._
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.{ Application, Configuration }
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.inject.guice.GuiceApplicationBuilder
import slick.jdbc.{ JdbcProfile, PostgresProfile }

import scala.concurrent.Await
import scala.concurrent.duration._

trait InMemoryDatabase extends AnyWordSpecLike with GuiceOneAppPerSuite {

  override def fakeApplication(): Application = {
    val builder = overrideDependencies(
      new GuiceApplicationBuilder()
        .configure(Configuration(ConfigFactory.load("application-test.conf")))
    )
    builder.build()
  }

  def overrideDependencies(application: GuiceApplicationBuilder): GuiceApplicationBuilder = {
    application
  }
}

trait DBConnectionProvider extends InMemoryDatabase with HasDatabaseConfigProvider[JdbcProfile] {
  import PostgresProfile.api._

  override lazy val dbConfigProvider: DatabaseConfigProvider =
    app.injector.instanceOf[DatabaseConfigProvider]

  implicit val dbConnection: DBConnection = new DBConnection(dbConfigProvider)
  implicit val jdbc = dbConnection.db

  private val createSchema = UserTable.userTable.schema ++
    UserProfileTable.userProfileTable.schema ++
    DishTable.dishTable.schema ++
    MenuTable.menuTable.schema ++
    MenuDishTable.menuDishTable.schema ++
    MenuPerDayTable.menuPerDayTable.schema ++
    MenuPerDayPerPersonTable.menuPerDayPerPersonTable.schema

  private val duration: Duration = 3 second

  def createTestSchema(): Unit = Await.result(jdbc.run(createSchema.create), duration)

  def dropTestSchema(): Unit = Await.result(jdbc.run(createSchema.drop), duration)
}
