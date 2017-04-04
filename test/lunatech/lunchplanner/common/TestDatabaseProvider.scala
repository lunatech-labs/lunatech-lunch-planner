package lunatech.lunchplanner.common

import lunatech.lunchplanner.persistence.{ DishTable, MenuTable, UserTable, MenuDishTable }
import slick.driver.PostgresDriver.api._
import slick.lifted.TableQuery

import scala.concurrent._
import scala.concurrent.duration._

trait TestDatabaseProvider {
  self: DBConnectionProvider =>

  private val defaultTimeout = 10.seconds

  val userTable: TableQuery[UserTable] = TableQuery[UserTable]
  val dishTable: TableQuery[DishTable] = TableQuery[DishTable]
  val menuTable: TableQuery[MenuTable] = TableQuery[MenuTable]
  val menuDishTable: TableQuery[MenuDishTable] = TableQuery[MenuDishTable]

  def cleanUserData(): Int = {
    Await.result(db.run(menuDishTable.delete), defaultTimeout)
    Await.result(db.run(userTable.delete), defaultTimeout)
    Await.result(db.run(dishTable.delete), defaultTimeout)
    Await.result(db.run(menuTable.delete), defaultTimeout)
  }

  def cleanDatabase(): Unit = {
    cleanUserData()
  }

}
