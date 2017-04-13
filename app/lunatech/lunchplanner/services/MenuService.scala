package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Menu
import lunatech.lunchplanner.persistence.MenuTable
import lunatech.lunchplanner.viewModels.MenuForm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuService @Inject() (implicit val connection: DBConnection) {

  def addNewMenu(menuForm: MenuForm): Future[Menu] = {
    val newMenu = Menu(name = menuForm.menuName)
    MenuTable.add(newMenu)
  }

  def getAllMenus: Future[Seq[Menu]] = MenuTable.getAll

  def getAllMenusUuidAndNames: Future[Seq[(String, String)]] = {
    val allMenus = getAllMenus
    allMenus.map(menuSeq =>
      menuSeq.map( menu => (menu.uuid.toString, menu.name))
    )
  }

  def getMenuByUuid(uuid: UUID): Future[Option[Menu]] = MenuTable.getByUUID(uuid)

  def insertOrUpdateMenu(menuUuid: UUID, menuForm: MenuForm): Future[Boolean] = {
    val menu = Menu(menuUuid, menuForm.menuName)
    MenuTable.insertOrUpdate(menu)
  }

  def deleteMenu(uuid: UUID): Future[Int] = MenuTable.remove(uuid)
}
