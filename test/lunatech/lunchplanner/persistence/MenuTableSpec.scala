package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models.Menu
import org.scalacheck._
import org.scalacheck.Prop._

import scala.concurrent.Await

object MenuTableSpec extends Properties(name = "MenuTable") with PropertyTestingConfig {

  import lunatech.lunchplanner.data.TableDataGenerator._

  property("add a new menu") = forAll { menu: Menu =>
    createTestSchema()
    
    val result = addMenuToDB(menu)

    dropTestSchema()

    result == menu
  }

  property("query for menus by uuid") = forAll { menu: Menu =>
    createTestSchema()
    
    addMenuToDB(menu)

    val result = Await.result(MenuTable.getByUUID(menu.uuid), defaultTimeout).get

    dropTestSchema()

    result == menu
  }

  property("query for menus by uuid") = forAll { (menu1: Menu, menu2: Menu) =>
    createTestSchema()
    
    addMenuToDB(menu1)
    addMenuToDB(menu2)

    val result = Await.result(MenuTable.getAll, defaultTimeout)

    dropTestSchema()

    result == Seq(menu1, menu2)
  }

  property("remove an existing menu by uuid") = forAll { menu: Menu =>
    createTestSchema()
    
    addMenuToDB(menu)

    val result = Await.result(MenuTable.removeByUuid(menu.uuid), defaultTimeout)
    val getByUuid = Await.result(MenuTable.getByUUID(menu.uuid), defaultTimeout).get

    dropTestSchema()

    result == 1 && getByUuid.isDeleted
  }

  property("not fail when trying to remove a menu that does not exist") = forAll { menu: Menu =>
    createTestSchema()
    
    // skip adding menu to DB

    val result = Await.result(MenuTable.removeByUuid(menu.uuid), defaultTimeout)

    dropTestSchema()

    result == 0
  }

  property("update an existing menu by uuid") = forAll { menu: Menu =>
    createTestSchema()
    
    addMenuToDB(menu)

    val menuUpdated = menu.copy(name = "updated name")
    val result = Await.result(MenuTable.update(menuUpdated), defaultTimeout)
    assert(result)

    val updatedMenu = Await.result(MenuTable.getByUUID(menu.uuid), defaultTimeout).get

    dropTestSchema()

    updatedMenu.name == "updated name"
  }

  private def addMenuToDB(menu: Menu): Menu = Await.result(MenuTable.add(menu), defaultTimeout)
}
