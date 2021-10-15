package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Menu
import lunatech.lunchplanner.persistence.MenuTable

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuService @Inject()(implicit val connection: DBConnection) {

  def add(menu: Menu): Future[Menu] = {
    MenuTable.add(menu)
  }

  def getAll: Future[Seq[Menu]] = MenuTable.getAll

  def getAllMenusUuidAndNames: Future[Seq[(String, String)]] = {
    val allMenus = getAll
    allMenus.map(menuSeq =>
      menuSeq.map(menu => (menu.uuid.toString, menu.name)))
  }

  def getByUuid(uuid: UUID): Future[Option[Menu]] = MenuTable.getByUUID(uuid)

  def update(menuUuid: UUID, menu: Menu): Future[Boolean] = {
    MenuTable.update(menu.copy(uuid = menuUuid))
  }

  def delete(uuid: UUID): Future[Int] = MenuTable.removeByUuid(uuid)
}
