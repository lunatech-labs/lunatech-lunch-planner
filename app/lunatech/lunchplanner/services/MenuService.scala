package lunatech.lunchplanner.services

import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.Menu
import lunatech.lunchplanner.persistence.MenuTable
import lunatech.lunchplanner.viewModels.MenuForm

import scala.concurrent.Future

class MenuService @Inject() (implicit val connection: DBConnection){

  def addNewMenu(menuForm: MenuForm): Future[Menu] = {
    val newMenu = Menu(name = menuForm.menuName)
    MenuTable.addMenu(newMenu)
  }
}
