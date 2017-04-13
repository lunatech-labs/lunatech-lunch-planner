package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ DishIsSelected, MenuDish, MenuWithAllDishesAndIsSelected, MenuWithDishes }
import lunatech.lunchplanner.persistence.MenuDishTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuDishService @Inject() (
  dishService: DishService,
  menuService: MenuService,
  implicit val connection: DBConnection){

  def add(menuDish: MenuDish): Future[MenuDish] = {
    MenuDishTable.add(menuDish)
  }

  def getAllWithListOfDishes: Future[Seq[MenuWithDishes]] = {
    val allMenus = menuService.getAll
    allMenus.flatMap {
      Future.traverse(_) { menu =>
        MenuDishTable.getByMenuUuid(menu.uuid)
          .flatMap(Future.traverse(_)(dish =>
            dishService.getByUuid(dish.dishUuid)).map(_.flatten))
          .map(dishes =>
            MenuWithDishes(menu.uuid, menu.name, dishes))
      }
    }
  }

  def deleteByMenuUuid(menuUuid: UUID): Future[Int] =
    MenuDishTable.removeByMenuUuid(menuUuid)

  def getByUuidWithSelectedDishes(menuUuid: UUID): Future[Option[MenuWithAllDishesAndIsSelected]] = {
    for{
      menuOption <- menuService.getByUuid(menuUuid)
      listDishes <- allIsSelectedInMenu(menuUuid)
    } yield menuOption.map(menu =>
      MenuWithAllDishesAndIsSelected(menuUuid, menu.name, listDishes))
  }

  private def allIsSelectedInMenu(menuUuid: UUID): Future[Seq[DishIsSelected]] = {
    dishService.getAll.flatMap(Future.traverse(_)( dish =>
      isSelectedInMenu(menuUuid, dish.uuid).map(isSelected =>
        DishIsSelected(dish.uuid, dish.name, isSelected))))
  }

  private def isSelectedInMenu(menuUuid: UUID, dishUuid: UUID): Future[Boolean] = {
    MenuDishTable.getByMenuUuid(menuUuid)
    .map(_.exists((dish: MenuDish) => dish.dishUuid == dishUuid))
  }
}
