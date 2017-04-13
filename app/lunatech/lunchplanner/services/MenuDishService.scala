package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ DishIsSelected, MenuDish, MenuWithAllDishesAndIsSelected, MenuWithDishes }
import lunatech.lunchplanner.persistence.MenuDishTable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaz._, Scalaz._

class MenuDishService @Inject() (
  dishService: DishService,
  menuService: MenuService,
  implicit val connection: DBConnection){

  def addNewMenuDish(menuUuid: UUID, dishUuid: UUID): Future[MenuDish] = {
    val newMenuDish = MenuDish(menuUuid = menuUuid, dishUuid = dishUuid)
    MenuDishTable.addMenuDish(newMenuDish)
  }

  def getAllMenusWithListOfDishes: Future[Seq[MenuWithDishes]] = {
    val allMenus = menuService.getAllMenus
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

  def deleteMenuDishesByMenuUuid(menuUuid: UUID): Future[Int] =
    MenuDishTable.removeMenuDishesByMenuUuid(menuUuid)

  def getMenuDishByUuidWithSelectedDishes(menuUuid: UUID): Future[Option[MenuWithAllDishesAndIsSelected]] = {
    for{
      menuOption <- menuService.getMenuByUuid(menuUuid)
      listDishes <- getListDishIsSelected(menuUuid)
    } yield menuOption.map(menu =>
      MenuWithAllDishesAndIsSelected(menuUuid, menu.name, listDishes))
  }

  private def getListDishIsSelected(menuUuid: UUID): Future[Seq[DishIsSelected]] = {
    dishService.getAllDishes.flatMap(Future.traverse(_)( dish =>
      getDishIsSelected(menuUuid, dish.uuid).map(isSelected =>
        DishIsSelected(dish.uuid, dish.name, isSelected))))
  }

  private def getDishIsSelected(menuUuid: UUID, dishUuid: UUID): Future[Boolean] = {
    MenuDishTable.getMenuDishByMenuUuid(menuUuid)
    .map(_.exists((dish: MenuDish) => dish.dishUuid == dishUuid))
  }
}
