package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ Menu, MenuWithDishes }
import lunatech.lunchplanner.persistence.{ MenuDishTable, MenuTable }
import lunatech.lunchplanner.viewModels.MenuForm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuService @Inject() (dishService: DishService, implicit val connection: DBConnection) {

  def addNewMenu(menuForm: MenuForm): Future[Menu] = {
    val newMenu = Menu(name = menuForm.menuName)
    MenuTable.addMenu(newMenu)
  }

  def getAllMenus: Future[Seq[Menu]] = MenuTable.getAllMenus

  def getAllMenusWithListOfDishes: Future[Seq[MenuWithDishes]] = {
    val allMenus = MenuTable.getAllMenus
    allMenus.flatMap {
      Future.traverse(_) { menu =>
        MenuDishTable.getMenuDishByMenuUuid(menu.uuid)
        .flatMap(Future.traverse(_)(dish =>
          dishService.getDishByUuid(dish.dishUuid)).map(_.flatten))
        .map(dishes =>
          MenuWithDishes(menu.uuid, menu.name, dishes))
      }
    }
}

  def getAllMenusUuidAndNames: Future[Seq[(String, String)]] = {
    val allMenus = getAllMenus
    allMenus.map(menuSeq =>
      menuSeq.map( menu => (menu.uuid.toString, menu.name))
    )
  }

  def getMenuByUuid(uuid: UUID): Future[Option[Menu]] = MenuTable.getMenuByUUID(uuid)
}
