package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models.{Dish, Menu, MenuDish}
import org.scalacheck.Properties
import org.scalacheck.Prop._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

object MenuDishTableSpec
    extends Properties(name = "MenuDish")
    with PropertyTestingConfig {

  import lunatech.lunchplanner.data.TableDataGenerator._

  property("add a new menu dish") = forAll {
    (dish: Dish, menu: Menu, menuDish: MenuDish) =>
      createTestSchema()

      addMenuAndDishToDB(dish, menu)
      val menuDishToAdd =
        menuDish.copy(dishUuid = dish.uuid, menuUuid = menu.uuid)
      val result =
        Await.result(MenuDishTable.add(menuDishToAdd), defaultTimeout)

      dropTestSchema()

      result == menuDishToAdd
  }

  property("query for menus dishes by uuid") = forAll {
    (dish: Dish, menu: Menu, menuDish: MenuDish) =>
      createTestSchema()

      val menuDishToAdd = addMenuAndDishAndMenuDishToDB(dish, menu, menuDish)

      val result = Await
        .result(MenuDishTable.getByUuid(menuDishToAdd.uuid), defaultTimeout)
        .get

      dropTestSchema()

      result == menuDishToAdd
  }

  property("query for menus dishes by menu uuid") = forAll {
    (dish: Dish, menu: Menu, menuDish: MenuDish) =>
      createTestSchema()

      val menuDishToAdd = addMenuAndDishAndMenuDishToDB(dish, menu, menuDish)

      val result =
        Await.result(MenuDishTable.getByMenuUuid(menu.uuid), defaultTimeout)

      dropTestSchema()

      result == Seq(menuDishToAdd)
  }

  property("query for menus dishes by non existent menu uuid") = forAll {
    menuDish: MenuDish =>
      createTestSchema()

      // skipping adding menuDish to DB

      val result =
        Await.result(MenuDishTable.getByMenuUuid(menuDish.uuid), defaultTimeout)

      dropTestSchema()

      result.isEmpty
  }

  property("query all menus dishes") = forAll {
    (dish: Dish, menu: Menu, menuDish1: MenuDish, menuDish2: MenuDish) =>
      createTestSchema()

      addMenuAndDishToDB(dish, menu)

      val menuDishToAdd1 =
        menuDish1.copy(dishUuid = dish.uuid, menuUuid = menu.uuid)
      val menuDishToAdd2 =
        menuDish2.copy(dishUuid = dish.uuid, menuUuid = menu.uuid)
      Await.result(MenuDishTable.add(menuDishToAdd1), defaultTimeout)
      Await.result(MenuDishTable.add(menuDishToAdd2), defaultTimeout)

      val result = Await.result(MenuDishTable.getAll, defaultTimeout)

      dropTestSchema()

      result == Seq(menuDishToAdd1, menuDishToAdd2)
  }

  property("remove an existing menu dish by uuid") = forAll {
    (dish: Dish, menu: Menu, menuDish: MenuDish) =>
      createTestSchema()

      val menuDishAdded = addMenuAndDishAndMenuDishToDB(dish, menu, menuDish)

      val result = Await.result(
        MenuDishTable.removeByUuid(menuDishAdded.uuid),
        defaultTimeout
      )
      val getByUuid = Await
        .result(MenuDishTable.getByUuid(menuDishAdded.uuid), defaultTimeout)
        .get

      dropTestSchema()

      result == 1 && getByUuid.isDeleted
  }

  property("not fail when trying to remove a menu that does not exist") =
    forAll { menuDish: MenuDish =>
      createTestSchema()
      // skipping adding menu to DB

      val result =
        Await.result(MenuDishTable.removeByUuid(menuDish.uuid), defaultTimeout)

      dropTestSchema()

      result == 0
    }

  property("remove an existing menu dish by menu uuid") = forAll {
    (dish: Dish, menu: Menu, menuDish: MenuDish) =>
      createTestSchema()

      addMenuAndDishAndMenuDishToDB(dish, menu, menuDish)

      val result =
        Await.result(MenuDishTable.removeByMenuUuid(menu.uuid), defaultTimeout)

      dropTestSchema()

      result == 1
  }

  property("remove an existing menu dish by dish uuid") = forAll {
    (dish: Dish, menu: Menu, menuDish: MenuDish) =>
      createTestSchema()

      addMenuAndDishAndMenuDishToDB(dish, menu, menuDish)

      val result =
        Await.result(MenuDishTable.removeByDishUuid(dish.uuid), defaultTimeout)

      dropTestSchema()

      result == 1
  }

  private def addMenuAndDishToDB(dish: Dish, menu: Menu): Menu = {
    val query = for {
      _         <- DishTable.add(dish)
      menuAdded <- MenuTable.add(menu)
    } yield menuAdded

    Await.result(query, defaultTimeout)
  }

  private def addMenuAndDishAndMenuDishToDB(
      dish: Dish,
      menu: Menu,
      menuDish: MenuDish
  ): MenuDish = {
    val menuDishToAdd =
      menuDish.copy(dishUuid = dish.uuid, menuUuid = menu.uuid)

    val query = for {
      _             <- DishTable.add(dish)
      _             <- MenuTable.add(menu)
      addedMenuDish <- MenuDishTable.add(menuDishToAdd)
    } yield addedMenuDish

    Await.result(query, defaultTimeout)
  }
}
