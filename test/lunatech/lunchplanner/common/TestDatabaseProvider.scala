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

<<<<<<< 4fb7f0b808c5ae80716341b274000c067e1c0ff4
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
=======
  def cleanDatabase: Int = {
    Await.result(jdbc.run(menuPerDayPerPersonTable.delete), defaultTimeout)
    Await.result(jdbc.run(menuPerDayTable.delete), defaultTimeout)
    Await.result(jdbc.run(menuDishTable.delete), defaultTimeout)
    Await.result(jdbc.run(dishTable.delete), defaultTimeout)
    Await.result(jdbc.run(menuTable.delete), defaultTimeout)
    Await.result(jdbc.run(userProfileTable.delete), defaultTimeout)
    Await.result(jdbc.run(userTable.delete), defaultTimeout)
  }

  def cleanUserAndProfileTable: Int = {
    Await.result(jdbc.run(userProfileTable.delete), defaultTimeout)
    Await.result(jdbc.run(userTable.delete), defaultTimeout)
>>>>>>> 142 - Replace unit test in package persistence by property based testing
  }

  def cleanMenuTable: Int = Await.result(jdbc.run(menuTable.delete), defaultTimeout)

  def cleanDishTable: Int = Await.result(jdbc.run(dishTable.delete), defaultTimeout)

<<<<<<< 4fb7f0b808c5ae80716341b274000c067e1c0ff4
  def cleanMenuDishTable: Unit = {
    Await.result(
      for {
        _ <- jdbc.run(menuDishTable.delete)
        _ <- jdbc.run(dishTable.delete)
      } yield (), defaultTimeout)
  }

  def cleanMenuPerDayTable: Unit = {
=======
  def cleanMenuDishTable: Int = {
    Await.result(jdbc.run(menuDishTable.delete), defaultTimeout)
    Await.result(jdbc.run(dishTable.delete), defaultTimeout)
  }

  def cleanMenuPerDayTable: Int = {
>>>>>>> 142 - Replace unit test in package persistence by property based testing
    Await.result(jdbc.run(menuPerDayTable.delete), defaultTimeout)
    cleanMenuDishTable
  }

<<<<<<< 4fb7f0b808c5ae80716341b274000c067e1c0ff4
  def cleanMenuPerDayPerPersonTable: Unit = {
=======
  def cleanMenuPerDayPerPersonTable: Int = {
>>>>>>> 142 - Replace unit test in package persistence by property based testing
    Await.result(jdbc.run(menuPerDayPerPersonTable.delete), defaultTimeout)
    cleanMenuPerDayTable
    cleanUserAndProfileTable
  }
}
