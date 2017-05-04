package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.MenuDish
import lunatech.lunchplanner.persistence.MenuDishTable

import scala.concurrent.Future

class MenuDishService @Inject() (implicit val connection: DBConnection){

  def addNewMenuDish(menuUuid: UUID, dishUuid: UUID): Future[MenuDish] = {
    val newMenuDish = MenuDish(menuUuid = menuUuid, dishUuid = dishUuid)
    MenuDishTable.addMenuDish(newMenuDish)
  }
}
