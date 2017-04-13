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

  def add(menuForm: MenuForm): Future[Menu] = {
    val newMenu = Menu(name = menuForm.menuName)
    MenuTable.add(newMenu)
  }

  def getAll: Future[Seq[Menu]] = MenuTable.getAll

  def getAllMenusUuidAndNames: Future[Seq[(String, String)]] = {
    val allMenus = getAll
    allMenus.map(menuSeq =>
      menuSeq.map( menu => (menu.uuid.toString, menu.name))
    )
  }

  def getByUuid(uuid: UUID): Future[Option[Menu]] = MenuTable.getByUUID(uuid)

  def insertOrUpdate(menuUuid: UUID, menuForm: MenuForm): Future[Boolean] = {
    val menu = Menu(menuUuid, menuForm.menuName)
    MenuTable.insertOrUpdate(menu)
  }

  def delete(uuid: UUID): Future[Int] = MenuTable.remove(uuid)
}
