package lunatech.lunchplanner.common

import lunatech.lunchplanner.models.User
import lunatech.lunchplanner.persistence.UserTable
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import scala.concurrent._
import scala.concurrent.duration.Duration


trait TestDatabaseProvider {
  self: DBConnectionProvider =>

  val userTable: TableQuery[UserTable] = TableQuery[UserTable]

  def cleanUserData(): Int = {
    Await.result(db.run(userTable.delete), Duration.Inf)
  }

  def cleanDatabase(): Unit = {
    cleanUserData()
  }

}
