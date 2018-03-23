package lunatech.lunchplanner.common

import lunatech.lunchplanner.persistence.{ DishTable, MenuDishTable, MenuPerDayPerPersonTable, MenuPerDayTable, MenuTable, UserProfileTable, UserTable }
import slick.jdbc.PostgresProfile.api._
import slick.lifted.TableQuery

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait TestDatabaseProvider {
  self: DBConnectionProvider =>

  val defaultTimeout: FiniteDuration = 10.seconds

  val userProfileTable: TableQuery[UserProfileTable] = TableQuery[UserProfileTable]
  val userTable: TableQuery[UserTable] = TableQuery[UserTable]
  val dishTable: TableQuery[DishTable] = TableQuery[DishTable]
  val menuTable: TableQuery[MenuTable] = TableQuery[MenuTable]
  val menuDishTable: TableQuery[MenuDishTable] = TableQuery[MenuDishTable]
  val menuPerDayTable: TableQuery[MenuPerDayTable] = TableQuery[MenuPerDayTable]
  val menuPerDayPerPersonTable: TableQuery[MenuPerDayPerPersonTable] = TableQuery[MenuPerDayPerPersonTable]

  def cleanDatabase: Unit = {
    Await.result(
      for {
        _ <- jdbc.run(menuPerDayPerPersonTable.delete)
        _ <- jdbc.run(menuPerDayTable.delete)
        _ <- jdbc.run(menuDishTable.delete)
        _ <- jdbc.run(dishTable.delete)
        _ <- jdbc.run(menuTable.delete)
        _ <- jdbc.run(userProfileTable.delete)
        _ <- jdbc.run(userTable.delete)
      } yield (), defaultTimeout)
  }

  def cleanUserAndProfileTable: Unit = {
    Await.result(
      for {
      _ <- jdbc.run(userProfileTable.delete)
      _ <- jdbc.run(userTable.delete)
    } yield (), defaultTimeout)
  }

  def cleanMenuTable: Int = Await.result(jdbc.run(menuTable.delete), defaultTimeout)

  def cleanDishTable: Int = Await.result(jdbc.run(dishTable.delete), defaultTimeout)

  def cleanMenuDishTable: Unit = {
    Await.result(
      for {
        _ <- jdbc.run(menuDishTable.delete)
        _ <- jdbc.run(dishTable.delete)
      } yield (), defaultTimeout)
  }

  def cleanMenuPerDayTable: Unit = {
    Await.result(jdbc.run(menuPerDayTable.delete), defaultTimeout)
    cleanMenuDishTable
  }

  def cleanMenuPerDayPerPersonTable: Unit = {
    Await.result(jdbc.run(menuPerDayPerPersonTable.delete), defaultTimeout)
    cleanMenuPerDayTable
    cleanUserAndProfileTable
  }
}
