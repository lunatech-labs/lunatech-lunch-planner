package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.{ AcceptanceSpec, DBConnection, TestDatabaseProvider }
import lunatech.lunchplanner.models.Menu

import scala.concurrent.Await
import scala.concurrent.duration._

class MenuTableSpec extends AcceptanceSpec with TestDatabaseProvider {

  implicit private val dbConnection = app.injector.instanceOf[DBConnection]
  private val defaultTimeout = 10.seconds

  override def beforeAll {
    cleanDatabase()
  }

  private val newMenu = Menu(name = "Main menu")

  "A Menu table" must {
    "add a new menu" in {
      val result = Await.result(MenuTable.addMenu(newMenu), defaultTimeout)
      result mustBe newMenu
    }

    "query for existing menus successfully" in {
      val result = Await.result(MenuTable.menuExists(newMenu.uuid), defaultTimeout)
      result mustBe true
    }

    "query for menus by uuid" in {
      val result = Await.result(MenuTable.getMenuByUUID(newMenu.uuid), defaultTimeout)
      result mustBe Some(newMenu)
    }

    "query all menus" in {
      val result = Await.result(MenuTable.getAllMenus, defaultTimeout)
      result mustBe Vector(newMenu)
    }

    "remove an existing menu by uuid" in {
      val result = Await.result(MenuTable.removeMenu(newMenu.uuid), defaultTimeout)
      result mustBe 1
    }

    "not fail when trying to remove a menu that does not exist" in {
      val result = Await.result(MenuTable.removeMenu(UUID.randomUUID()), defaultTimeout)
      result mustBe 0
    }
  }
}
