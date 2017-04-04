package lunatech.lunchplanner.common

import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.{ DishTable, UserTable }
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import scala.concurrent._
import scala.concurrent.duration.Duration


trait TestDatabaseProvider {
  self: DBConnectionProvider =>

  val userTable: TableQuery[UserTable] = TableQuery[UserTable]
  val dishTable: TableQuery[DishTable] = TableQuery[DishTable]

  def cleanUserData(): Int = {
    Await.result(db.run(userTable.delete), Duration.Inf)
    Await.result(db.run(dishTable.delete), Duration.Inf)
  }

  def cleanDatabase(): Unit = {
    cleanUserData()
  }

}
