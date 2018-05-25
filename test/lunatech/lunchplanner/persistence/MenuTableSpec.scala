package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models.Menu
import org.scalacheck._
import org.scalacheck.Prop._
import shapeless.contrib.scalacheck._

import scala.concurrent.Await

object MenuTableSpec extends Properties(name = "MenuTable") with PropertyTestingConfig {

  import lunatech.lunchplanner.data.TableDataGenerator._

  override def afterAll(): Unit = dbConnection.db.close()

  property("add a new menu") = forAll { menu: Menu =>
    val result = addMenuToDB(menu)

    cleanMenuTable

    result == menu
  }

  property("query for menus by uuid") = forAll { menu: Menu =>
    addMenuToDB(menu)

    val result = Await.result(MenuTable.getByUUID(menu.uuid), defaultTimeout).get

    cleanMenuTable

    result == menu
  }

  property("query for menus by uuid") = forAll { (menu1: Menu, menu2: Menu) =>
    addMenuToDB(menu1)
    addMenuToDB(menu2)

    val result = Await.result(MenuTable.getAll, defaultTimeout)

    cleanMenuTable

    result == Seq(menu1, menu2)
  }

  property("remove an existing menu by uuid") = forAll { menu: Menu =>
    addMenuToDB(menu)

    val result = Await.result(MenuTable.removeByUuid(menu.uuid), defaultTimeout)
    val getByUuid = Await.result(MenuTable.getByUUID(menu.uuid), defaultTimeout).get

    result == 1 && getByUuid.isDeleted
  }

  property("not fail when trying to remove a menu that does not exist") = forAll { menu: Menu =>
    // skip adding menu to DB

    val result = Await.result(MenuTable.removeByUuid(menu.uuid), defaultTimeout)
    result == 0
  }

  property("update an existing menu by uuid") = forAll { menu: Menu =>
    addMenuToDB(menu)

    val menuUpdated = menu.copy(name = "updated name")
    val result = Await.result(MenuTable.insertOrUpdate(menuUpdated), defaultTimeout)
    assert(result)

    val updatedMenu = Await.result(MenuTable.getByUUID(menu.uuid), defaultTimeout).get

    cleanMenuTable

    updatedMenu.name == "updated name"
  }

  private def addMenuToDB(menu: Menu): Menu = Await.result(MenuTable.add(menu), defaultTimeout)
}
