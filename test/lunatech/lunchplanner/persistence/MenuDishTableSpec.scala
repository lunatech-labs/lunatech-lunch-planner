package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.{ AcceptanceSpec, DBConnection, TestDatabaseProvider }
import lunatech.lunchplanner.models.{ Dish, Menu, MenuDish }

import scala.concurrent.Await
import scala.concurrent.duration._

class MenuDishTableSpec extends AcceptanceSpec with TestDatabaseProvider {

  implicit private val dbConnection = app.injector.instanceOf[DBConnection]
  private val defaultTimeout = 10.seconds

  private val newMenu = Menu(name = "Main menu")
  private val pastaBologneseDish = Dish(
    name = "pasta bolognese",
    description = "pasta bolognese for 2 people",
    hasBeef = true,
    remarks = Some("favorite dish of person A")
  )
  private val vegetarianDish = Dish(
    name = "vegetarian",
    description = "warm vegetarian food from fancy restaurant",
    isVegetarian = true,
    isGlutenFree = true,
    hasLactose = true
  )

  private val newMenuDish = MenuDish (
    menuUuid = newMenu.uuid,
    dishUuid = pastaBologneseDish.uuid
  )

  private val newMenuDishVegetarian = MenuDish (
    menuUuid = newMenu.uuid,
    dishUuid = vegetarianDish.uuid
  )

  override def beforeAll {
    cleanDatabase()

    Await.result(MenuTable.addMenu(newMenu), defaultTimeout)
    Await.result(DishTable.addDish(pastaBologneseDish), defaultTimeout)
    Await.result(DishTable.addDish(vegetarianDish), defaultTimeout)
  }

  "A MenuDish table" must {
    "add a new menu dish" in {
      val result = Await.result(MenuDishTable.addMenuDish(newMenuDish), defaultTimeout)
      result mustBe newMenuDish
    }

    "query for existing menus dishes successfully" in {
      val result = Await.result(MenuDishTable.menuDishExists(newMenuDish.uuid), defaultTimeout)
      result mustBe true
    }

    "query for menus dishes by uuid" in {
      val result = Await.result(MenuDishTable.getMenuDishByUUID(newMenuDish.uuid), defaultTimeout)
      result mustBe Some(newMenuDish)
    }

    "query for menus dishes by menu uuid" in {
      Await.result(MenuDishTable.addMenuDish(newMenuDishVegetarian), defaultTimeout)

      val result = Await.result(MenuDishTable.getMenuDishByMenuUuid(newMenu.uuid), defaultTimeout)
      result mustBe Vector(newMenuDish, newMenuDishVegetarian)
    }

    "query for menus dishes by non existent menu uuid" in {
      val result = Await.result(MenuDishTable.getMenuDishByMenuUuid(UUID.randomUUID()), defaultTimeout)
      result mustBe Vector()
    }

    "query all menus dishes" in {
      val result = Await.result(MenuDishTable.getAllMenuDishes, defaultTimeout)
      result mustBe Vector(newMenuDish, newMenuDishVegetarian)
    }

    "remove an existing menu dish by uuid" in {
      val result = Await.result(MenuDishTable.removeMenuDish(newMenuDish.uuid), defaultTimeout)
      result mustBe 1
    }

    "not fail when trying to remove a menu that does not exist" in {
      val result = Await.result(MenuDishTable.removeMenuDish(UUID.randomUUID()), defaultTimeout)
      result mustBe 0
    }
  }
}
