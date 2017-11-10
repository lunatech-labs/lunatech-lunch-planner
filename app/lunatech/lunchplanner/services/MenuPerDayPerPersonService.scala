package lunatech.lunchplanner.services

import java.sql.Date
import java.text.SimpleDateFormat
import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models._
import lunatech.lunchplanner.persistence.{MenuDishTable, MenuPerDayPerPersonTable}

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

  def getAllUpcomingSchedulesByUser(userUuid: UUID) : Future[Seq[MenuPerDayPerPerson]] = MenuPerDayPerPersonTable.getAllUpcomingSchedulesByUser(userUuid)

  def getAll(userUuid: UUID): Future[Seq[MenuWithNamePerDayPerPerson]]  = {
    val allMenusWithNamePerDay = getAllMenuWithNamePerDay

    allMenusWithNamePerDay.flatMap {
      Future.traverse(_) { menuWithNamePerDay =>
        getByUserUuidAndMenuPerDayUuid(userUuid, menuWithNamePerDay.uuid).map{ menuPerDayPerPerson =>
          MenuWithNamePerDayPerPerson(menuWithNamePerDay.uuid,
                                      menuWithNamePerDay.menuDate,
                                      menuWithNamePerDay.menuName,
                                      userUuid,
                                      isAttending(menuPerDayPerPerson),
                                      menuWithNamePerDay.location)
        }
      }
    }
  }

  def getAllMenuWithNamePerDayWithDishesPerPerson(userUuid: UUID): Future[Seq[MenuWithNameWithDishesPerPerson]] = {
    getAllMenuWithNamePerDay.flatMap { allMenuWithNamePerDay =>
      Future.traverse(allMenuWithNamePerDay) { menuWithNamePerDay =>
        MenuDishTable.getByMenuUuid(menuWithNamePerDay.menuUuid).flatMap(menuDishes =>
          Future.traverse(menuDishes)(dish => dishService.getByUuid(dish.dishUuid)).map(a => a.flatten)).flatMap { dishes =>
          getByUserUuidAndMenuPerDayUuid(userUuid, menuWithNamePerDay.uuid).map { menuPerDayPerPerson =>
              MenuWithNameWithDishesPerPerson(menuWithNamePerDay.uuid,
                                              menuWithNamePerDay.menuDate,
                                              menuWithNamePerDay.menuName,
                                              dishes,
                                              userUuid,
                                              isAttending(menuPerDayPerPerson),
                                              menuWithNamePerDay.location)
          }
        }
      }
    }
  }

  def getByUserUuidAndMenuPerDayUuid(userUuid: UUID, menuPerDayUuid: UUID): Future[Option[MenuPerDayPerPerson]] =
    MenuPerDayPerPersonTable.getByUserUuidAndMenuPerDayUuid(userUuid, menuPerDayUuid)

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
                  numberOfPeopleSignedIn = count,
                  menuPerDay.location)))
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
            getAttendeeCountByMenuPerDayUuid(menuPerDay.uuid)
              .map(count =>
                Some(MenuWithNamePerDay(
                  menuPerDay.uuid,
                  menuData.uuid,
                  new SimpleDateFormat("dd-MM-yyyy").format(menuPerDay.date),
                  menuData.name,
                  numberOfPeopleSignedIn = count,
                  menuPerDay.location)))
          case None => Future.successful(None)
        }
      }
    }.map(_.flatten)
  }

  def deleteByMenuPerPersonUuid(menuPerDayUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.removeByMenuPerDayUuid(menuPerDayUuid)

  def getListOfPeopleByMenuPerDay(menuPerDayUuid: UUID): Future[Seq[MenuPerDayAttendant]] = {
    MenuPerDayPerPersonTable.getAttendeesByMenuPerDayUuid(menuPerDayUuid)
      .map(_.map(user => MenuPerDayAttendant(user._1.name, user._2.otherRestriction.getOrElse(""))))
  }

  def getListOfPeopleByMenuPerDayForReport(menuPerDay: MenuPerDay): Future[Seq[MenuPerDayReport]] = {
    MenuPerDayPerPersonTable.getAttendeesByMenuPerDayUuid(menuPerDay.uuid)
      .map(_.map(user => MenuPerDayReport(user._1.name, menuPerDay.date)))
  }

  def getListOfPeopleByMenuPerDayByLocationAndDateForReport(menuPerDay: MenuPerDay): Future[Seq[MenuPerDayReportByDateAndLocation]] = {
    MenuPerDayPerPersonTable.getAttendeesByMenuPerDayUuid(menuPerDay.uuid)
      .map(_.map(user => MenuPerDayReportByDateAndLocation(menuPerDay.date, menuPerDay.location, user._1.name)))
  }

  def getNotAttendingByDate(date: Date): Future[Seq[MenuPerDayReport]] = {
    MenuPerDayPerPersonTable.getNotAttendingByDate(date)
      .map(_.map(user => MenuPerDayReport(user._1.name, date)))
  }

  private def isAttending(menuPerDayPerPerson: Option[MenuPerDayPerPerson]): Option[Boolean] = {
    menuPerDayPerPerson.map(_.isAttending)
  }

  def getAttendeesEmailAddressesForUpcomingLunch: Future[Seq[String]] = {
    MenuPerDayPerPersonTable.getAttendeesEmailAddressesForUpcomingLunch
  }

  private def getNumberOfMenusPerDayPerPersonByMenuPerDay(menuPerDayUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.getByMenuPerDayUuid(menuPerDayUuid).map(_.length)

  private def getAttendeeCountByMenuPerDayUuid(menuPerDayUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.getAttendeeCountByMenuPerDayUuid(menuPerDayUuid)

}
