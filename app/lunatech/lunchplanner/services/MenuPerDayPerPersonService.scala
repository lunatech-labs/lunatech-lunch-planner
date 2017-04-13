package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ MenuPerDay, MenuPerDayPerPerson, MenuWithDishes, MenuWithNamePerDay, MenuWithNamePerDayPerPerson, MenuWithNamePerDayWithDishesPerPerson }
import lunatech.lunchplanner.persistence.{ MenuDishTable, MenuPerDayPerPersonTable, MenuPerDayTable }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayPerPersonService  @Inject() (
  dishService: DishService,
  menuService: MenuService,
  menuPerDayService: MenuPerDayService,
  implicit val connection: DBConnection) {

  def addNewMenusPerDayPerPerson(menuPerDayPerPerson: MenuPerDayPerPerson): Future[MenuPerDayPerPerson] =
    MenuPerDayPerPersonTable.add(menuPerDayPerPerson)

  def getAllMenusPerDayPerPersonByUserUuid(userUuid: UUID): Future[Seq[MenuPerDayPerPerson]] = MenuPerDayPerPersonTable.getByUserUuid(userUuid)

  def getAllMenuWithNamePerDayPerPerson(userUuid: UUID): Future[Seq[MenuWithNamePerDayPerPerson]]  = {
    val allMenusWithNamePerDay = getAllMenuWithNamePerDay

    allMenusWithNamePerDay.flatMap {
      Future.traverse(_) { menuWithNamePerDay =>
        val isMenuSelected = isMenuPerDaySelectedForPerson(userUuid, menuWithNamePerDay.uuid)
        isMenuSelected.map( isSelected =>
        MenuWithNamePerDayPerPerson(menuWithNamePerDay.uuid, menuWithNamePerDay.menuDateAndName, userUuid, isSelected))
      }
    }
  }

  def getAllMenuWithNamePerDayWithDishesPerPerson(userUuid: UUID): Future[Seq[MenuWithNamePerDayWithDishesPerPerson]]  = {
    val allMenusWithNamePerDay = getAllMenuWithNamePerDay

    allMenusWithNamePerDay.flatMap {
      Future.traverse(_) { menuWithNamePerDay =>
        MenuDishTable.getByMenuUuid(menuWithNamePerDay.menuUuid)
          .flatMap(Future.traverse(_)(dish =>
            dishService.getDishByUuid(dish.dishUuid)).map(_.flatten))
          .flatMap { dishes =>
            val isMenuSelected = isMenuPerDaySelectedForPerson(userUuid, menuWithNamePerDay.uuid)
            isMenuSelected.map(isSelected =>
              MenuWithNamePerDayWithDishesPerPerson(menuWithNamePerDay.uuid, menuWithNamePerDay.menuDateAndName, dishes, userUuid, isSelected))
          }
      }
    }
  }

  def isMenuPerDaySelectedForPerson(userUuid: UUID, menuPerDayUuid: UUID): Future[Boolean] =
    MenuPerDayPerPersonTable.getByUserUuidAndMenuPerDayUuid(userUuid, menuPerDayUuid).map(_.isDefined)

  def removeMenuPerDayPerPerson(menuPerDayPerPersonUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.remove(menuPerDayPerPersonUuid)

  def getNumberOfMenusPerDayPerPersonForMenuPerDay(menuPerDayUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.getByMenuPerDayUuid(menuPerDayUuid).map(_.length)

  def getAllMenuWithNamePerDay: Future[Seq[MenuWithNamePerDay]] = {
    val allMenusPerDay = menuPerDayService.getAllMenusPerDay

    allMenusPerDay.flatMap {
      Future.traverse(_) { menuPerDay =>
        val menu = menuService.getMenuByUuid(menuPerDay.menuUuid)
        menu.flatMap {
          case Some(menuData) => {
              getNumberOfMenusPerDayPerPersonForMenuPerDay(menuPerDay.uuid)
              .map(count =>
                MenuWithNamePerDay(menuPerDay.uuid, menuData.uuid, menuPerDay.date.toString + "  " + menuData.name, numberOfPeopleSignedIn = count))
            }
          case None => ???
        }
      }
    }
  }

  def deleteMenusPerDayPerPersonByMenuPerPersonUuid(menuPerDayUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.removeByMenuPerDayUuid(menuPerDayUuid)
}
