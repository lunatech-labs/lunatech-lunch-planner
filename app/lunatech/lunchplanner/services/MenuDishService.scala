package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{
  Dish,
  DishIsSelected,
  MenuDish,
  MenuWithAllDishesAndIsSelected,
  MenuWithDishes
}
import lunatech.lunchplanner.persistence.MenuDishTable
import java.util.UUID
import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuDishService @Inject() (
    dishService: DishService,
    menuService: MenuService
)(implicit val connection: DBConnection) {

  def add(menuDish: MenuDish): Future[MenuDish] =
    MenuDishTable.add(menuDish)

  def getMenuListOfDishes(menuUuid: UUID): Future[Seq[Dish]] =
    for {
      menuDishes: Seq[MenuDish] <- MenuDishTable.getByMenuUuid(menuUuid)
      dishes <- Future
        .sequence(
          menuDishes.map(dish => dishService.getByUuid(dish.dishUuid))
        )
        .map(_.flatten)
    } yield dishes

  def getAllWithListOfDishes: Future[Seq[MenuWithDishes]] =
    for {
      allMenus <- menuService.getAll
      result <- Future.sequence(allMenus.map { menu =>
        for {
          menuDishes <- MenuDishTable.getByMenuUuid(menu.uuid)
          dishes <- Future
            .sequence(
              menuDishes.map(dish => dishService.getByUuid(dish.dishUuid))
            )
            .map(_.flatten)
        } yield MenuWithDishes(menu.uuid, menu.name, dishes)
      })
    } yield result

  def deleteByMenuUuid(menuUuid: UUID): Future[Int] =
    MenuDishTable.removeByMenuUuid(menuUuid)

  def deleteByDishUuid(dishUuid: UUID): Future[Int] =
    MenuDishTable.removeByDishUuid(dishUuid)

  def getByUuidWithSelectedDishes(
      menuUuid: UUID
  ): Future[Option[MenuWithAllDishesAndIsSelected]] =
    for {
      menuOption <- menuService.getByUuid(menuUuid)
      listDishes <- allIsSelectedInMenu(menuUuid)
    } yield menuOption.map(menu =>
      MenuWithAllDishesAndIsSelected(menuUuid, menu.name, listDishes)
    )

  private def allIsSelectedInMenu(
      menuUuid: UUID
  ): Future[Seq[DishIsSelected]] =
    dishService.getAll.flatMap(
      Future.traverse(_)(dish =>
        isSelectedInMenu(menuUuid, dish.uuid).map(isSelected =>
          DishIsSelected(dish.uuid, dish.name, isSelected)
        )
      )
    )

  private def isSelectedInMenu(
      menuUuid: UUID,
      dishUuid: UUID
  ): Future[Boolean] =
    MenuDishTable
      .getByMenuUuid(menuUuid)
      .map(_.exists((dish: MenuDish) => dish.dishUuid == dishUuid))
}
