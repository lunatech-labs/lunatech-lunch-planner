package lunatech.lunchplanner.services

import java.text.SimpleDateFormat
import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ MenuPerDay, MenuPerDayAttendant, MenuPerDayPerPerson, MenuPerDayReport, MenuWithNamePerDay, MenuWithNamePerDayPerPerson, MenuWithNameWithDishesPerPerson }
import lunatech.lunchplanner.persistence.{ MenuDishTable, MenuPerDayPerPersonTable }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayPerPersonService  @Inject() (
  dishService: DishService,
  menuService: MenuService,
  menuPerDayService: MenuPerDayService,
  implicit val connection: DBConnection) {

  def add(menuPerDayPerPerson: MenuPerDayPerPerson): Future[MenuPerDayPerPerson] =
    MenuPerDayPerPersonTable.add(menuPerDayPerPerson)

  def getAllByUserUuid(userUuid: UUID): Future[Seq[MenuPerDayPerPerson]] = MenuPerDayPerPersonTable.getByUserUuid(userUuid)

  def getAll(userUuid: UUID): Future[Seq[MenuWithNamePerDayPerPerson]]  = {
    val allMenusWithNamePerDay = getAllMenuWithNamePerDay

    allMenusWithNamePerDay.flatMap {
      Future.traverse(_) { menuWithNamePerDay =>
        isMenuPerDaySelectedForPerson(userUuid, menuWithNamePerDay.uuid)
          .map(isSelected =>
        MenuWithNamePerDayPerPerson(menuWithNamePerDay.uuid, menuWithNamePerDay.menuDate, menuWithNamePerDay.menuName, userUuid, isSelected))
      }
    }
  }

  def getAllMenuWithNamePerDayWithDishesPerPerson(userUuid: UUID): Future[Seq[MenuWithNameWithDishesPerPerson]]  = {
    getAllMenuWithNamePerDay
    .flatMap {
      Future.traverse(_) { menuWithNamePerDay =>
        MenuDishTable.getByMenuUuid(menuWithNamePerDay.menuUuid)
          .flatMap(Future.traverse(_)(dish =>
            dishService.getByUuid(dish.dishUuid)).map(_.flatten))
          .flatMap { dishes =>
            isMenuPerDaySelectedForPerson(userUuid, menuWithNamePerDay.uuid)
            .map(isSelected =>
              MenuWithNameWithDishesPerPerson(
                menuWithNamePerDay.uuid,
                menuWithNamePerDay.menuDate,
                menuWithNamePerDay.menuName,
                dishes,
                userUuid,
                isSelected))
          }
      }
    }
  }

  def isMenuPerDaySelectedForPerson(userUuid: UUID, menuPerDayUuid: UUID): Future[Boolean] =
    MenuPerDayPerPersonTable.getByUserUuidAndMenuPerDayUuid(userUuid, menuPerDayUuid).map(_.isDefined)

  def delete(menuPerDayPerPersonUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.remove(menuPerDayPerPersonUuid)

  def getAllMenuWithNamePerDay: Future[Seq[MenuWithNamePerDay]] = {
    menuPerDayService.getAllFutureAndOrderedByDate.flatMap {
      Future.traverse(_) { menuPerDay =>
        menuService.getByUuid(menuPerDay.menuUuid).flatMap {
          case Some(menuData) =>
              getNumberOfMenusPerDayPerPersonByMenuPerDay(menuPerDay.uuid)
              .map(count =>
                Some(MenuWithNamePerDay(
                  menuPerDay.uuid,
                  menuData.uuid,
                  new SimpleDateFormat("dd-MM-yyyy").format(menuPerDay.date),
                  menuData.name,
                  numberOfPeopleSignedIn = count)))
          case None => Future.successful(None)
        }
      }
    }.map(_.flatten)
  }

  def getAllMenuWithNamePerDayFilterDateRange(dateStart: java.sql.Date, dateEnd: java.sql.Date):
  Future[Seq[MenuWithNamePerDay]] = {
    menuPerDayService.getAllOrderedByDateFilterDateRange(dateStart, dateEnd).flatMap {
      Future.traverse(_) { menuPerDay =>
        menuService.getByUuid(menuPerDay.menuUuid).flatMap {
          case Some(menuData) =>
            getNumberOfMenusPerDayPerPersonByMenuPerDay(menuPerDay.uuid)
              .map(count =>
                Some(MenuWithNamePerDay(
                  menuPerDay.uuid,
                  menuData.uuid,
                  new SimpleDateFormat("dd-MM-yyyy").format(menuPerDay.date),
                  menuData.name,
                  numberOfPeopleSignedIn = count)))
          case None => Future.successful(None)
        }
      }
    }.map(_.flatten)
  }

  def deleteByMenuPerPersonUuid(menuPerDayUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.removeByMenuPerDayUuid(menuPerDayUuid)

  def getListOfPeopleByMenuPerDay(menuPerDayUuid: UUID): Future[Seq[MenuPerDayAttendant]] = {
    MenuPerDayPerPersonTable.getUsersByMenuPerDayUuid(menuPerDayUuid)
      .map(_.map(user => MenuPerDayAttendant(user._1.name, user._2.otherRestriction.getOrElse(""))))
  }

  def getListOfPeopleByMenuPerDayForReport(menuPerDay: MenuPerDay): Future[Seq[MenuPerDayReport]] = {
    MenuPerDayPerPersonTable.getUsersByMenuPerDayUuid(menuPerDay.uuid)
      .map(_.map(user => MenuPerDayReport(user._1.name, menuPerDay.date)))
  }

  private def getNumberOfMenusPerDayPerPersonByMenuPerDay(menuPerDayUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.getByMenuPerDayUuid(menuPerDayUuid).map(_.length)

}
