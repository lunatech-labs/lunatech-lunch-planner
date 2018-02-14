package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models.{ Dish, Menu, MenuDish }
import org.scalacheck.Properties
import org.scalacheck.Prop._

import scala.concurrent.Await
import shapeless.contrib.scalacheck._

object MenuDishTableSpec extends Properties("MenuDish") with PropertyTestingConfig {

  import TableDataGenerator._

  property("add a new menu dish") = forAll { (dish: Dish, menu: Menu, menuDish: MenuDish) =>
    addMenuAndDishToDB(dish, menu)
    val menuDishToAdd = menuDish.copy(dishUuid = dish.uuid, menuUuid = menu.uuid)
    val result = Await.result(MenuDishTable.add(menuDishToAdd), defaultTimeout)

    cleanMenuDishTableProps

    result == menuDishToAdd
  }

  property("query for existing menus dishes successfully") = forAll { (dish: Dish, menu: Menu, menuDish: MenuDish) =>
    val menuDishAdded = addMenuAndDishAndMenuDishToDB(dish, menu, menuDish)

    val result = Await.result(MenuDishTable.exists(menuDishAdded.uuid), defaultTimeout)

    cleanMenuDishTableProps
    result
  }

  property("query for menus dishes by uuid") = forAll { (dish: Dish, menu: Menu, menuDish: MenuDish) =>
    val menuDishToAdd = addMenuAndDishAndMenuDishToDB(dish, menu, menuDish)

    val result = Await.result(MenuDishTable.getByUuid(menuDishToAdd.uuid), defaultTimeout).get

    cleanMenuDishTableProps
    result == menuDishToAdd
  }

  property("query for menus dishes by menu uuid") = forAll { (dish: Dish, menu: Menu, menuDish: MenuDish) =>
    val menuDishToAdd = addMenuAndDishAndMenuDishToDB(dish, menu, menuDish)

    val result = Await.result(MenuDishTable.getByMenuUuid(menu.uuid), defaultTimeout)

    cleanMenuDishTableProps
    result == Seq(menuDishToAdd)
  }

  property("query for menus dishes by non existent menu uuid") = forAll { menuDish: MenuDish =>
    // skipping adding menuDish to DB

    val result = Await.result(MenuDishTable.getByMenuUuid(menuDish.uuid), defaultTimeout)
    result.isEmpty
  }

  property("query all menus dishes") = forAll {
    (dish: Dish, menu: Menu, menuDish1: MenuDish, menuDish2: MenuDish) =>
      addMenuAndDishToDB(dish, menu)

      val menuDishToAdd1 = menuDish1.copy(dishUuid = dish.uuid, menuUuid = menu.uuid)
      val menuDishToAdd2 = menuDish2.copy(dishUuid = dish.uuid, menuUuid = menu.uuid)
      Await.result(MenuDishTable.add(menuDishToAdd1), defaultTimeout)
      Await.result(MenuDishTable.add(menuDishToAdd2), defaultTimeout)

      val result = Await.result(MenuDishTable.getAll, defaultTimeout)

      cleanMenuDishTableProps
      result == Seq(menuDishToAdd1, menuDishToAdd2)
  }

  property("remove an existing menu dish by uuid") = forAll {
    (dish: Dish, menu: Menu, menuDish: MenuDish) =>
      val menuDishAdded = addMenuAndDishAndMenuDishToDB(dish, menu, menuDish)

      val result = Await.result(MenuDishTable.removeByUuid(menuDishAdded.uuid), defaultTimeout)
      result == 1
  }

  property("not fail when trying to remove a menu that does not exist") = forAll { menuDish: MenuDish =>
    // skipping adding menu to DB

    val result = Await.result(MenuDishTable.removeByUuid(menuDish.uuid), defaultTimeout)
    result == 0
  }

  property("remove an existing menu dish by menu uuid") = forAll {
    (dish: Dish, menu: Menu, menuDish: MenuDish) =>
      addMenuAndDishAndMenuDishToDB(dish, menu, menuDish)

      val result = Await.result(MenuDishTable.removeByMenuUuid(menu.uuid), defaultTimeout)
      result == 1
  }

  property("remove an existing menu dish by dish uuid") = forAll {
    (dish: Dish, menu: Menu, menuDish: MenuDish) =>
      addMenuAndDishAndMenuDishToDB(dish, menu, menuDish)

      val result = Await.result(MenuDishTable.removeByDishUuid(dish.uuid), defaultTimeout)
      result == 1
  }

  private def addMenuAndDishToDB(dish: Dish, menu: Menu) = {
    Await.result(DishTable.add(dish), defaultTimeout)
    Await.result(MenuTable.add(menu), defaultTimeout)
  }

  private def addMenuAndDishAndMenuDishToDB(dish: Dish, menu: Menu, menuDish: MenuDish): MenuDish = {
    addMenuAndDishToDB(dish, menu)

    val menuDishToAdd = menuDish.copy(dishUuid = dish.uuid, menuUuid = menu.uuid)
    Await.result(MenuDishTable.add(menuDishToAdd), defaultTimeout)
  }

  private def cleanMenuDishTableProps = {
    cleanMenuDishTable
    true
  }
}
