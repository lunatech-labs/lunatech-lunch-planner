package lunatech.lunchplanner.services

import java.sql.Date
import java.text.SimpleDateFormat
import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models._
import lunatech.lunchplanner.persistence.{
  MenuDishTable,
  MenuPerDayPerPersonTable
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayPerPersonService @Inject()(
    dishService: DishService,
    menuService: MenuService,
    menuPerDayService: MenuPerDayService)(
    implicit val connection: DBConnection) {

  def add(menuPerDayPerPerson: MenuPerDayPerPerson): Future[MenuPerDayPerPerson] =
    MenuPerDayPerPersonTable.add(menuPerDayPerPerson)

  def getAllByUserUuid(userUuid: UUID): Future[Seq[MenuPerDayPerPerson]] =
    MenuPerDayPerPersonTable.getByUserUuid(userUuid)

  def getAllUpcomingSchedulesByUser(
      userUuid: UUID): Future[Seq[MenuPerDayPerPerson]] =
    MenuPerDayPerPersonTable.getAllUpcomingSchedulesByUser(userUuid)

  def getAll(userUuid: UUID): Future[Seq[MenuWithNamePerDayPerPerson]] = {
    def getSchedule(menuWithNamePerDay: MenuWithNamePerDay): Future[MenuWithNamePerDayPerPerson] = {
      getByUserUuidAndMenuPerDayUuid(userUuid, menuWithNamePerDay.uuid).map { menuPerDayPerPerson =>
        MenuWithNamePerDayPerPerson(menuWithNamePerDay.uuid,
          menuWithNamePerDay.menuDate,
          menuWithNamePerDay.menuName,
          userUuid,
          isAttending(menuPerDayPerPerson),
          menuWithNamePerDay.location)
      }
    }

    for{
      allMenusWithNamePerDay <- getAllMenuWithNamePerDay
      result <- Future.traverse(allMenusWithNamePerDay)(getSchedule)
    } yield result
  }

  def getAllMenuWithNamePerDayWithDishesPerPerson(userUuid: UUID): Future[Seq[MenuWithNameWithDishesPerPerson]] = {
    def retrieveInfoForMenu(menuWithNamePerDay: MenuWithNamePerDay): Future[MenuWithNameWithDishesPerPerson] = {
      for {
        menuDishes <- MenuDishTable.getByMenuUuid(menuWithNamePerDay.menuUuid)
        dishes <- Future.traverse(menuDishes)(dish => dishService.getByUuid(dish.dishUuid)).map(_.flatten)
        menuPerDayPerPerson <- getByUserUuidAndMenuPerDayUuid(userUuid, menuWithNamePerDay.uuid)
      } yield MenuWithNameWithDishesPerPerson(
        menuWithNamePerDay.uuid,
        menuWithNamePerDay.menuDate,
        menuWithNamePerDay.menuName,
        dishes,
        userUuid,
        isAttending(menuPerDayPerPerson),
        menuWithNamePerDay.location
      )
    }

    for {
      allMenuWithNamePerDay <- getAllMenuWithNamePerDay
      result <- Future.traverse(allMenuWithNamePerDay)(retrieveInfoForMenu)
    } yield result
  }

  def getByUserUuidAndMenuPerDayUuid(userUuid: UUID, menuPerDayUuid: UUID): Future[Option[MenuPerDayPerPerson]] =
    MenuPerDayPerPersonTable.getByUserUuidAndMenuPerDayUuid(userUuid, menuPerDayUuid)

  def delete(menuPerDayPerPersonUuids: Seq[UUID]): Future[Seq[Int]] =
    Future.sequence {
      menuPerDayPerPersonUuids.map(MenuPerDayPerPersonTable.removeByUuid)
    }

  def getAllMenuWithNamePerDay: Future[Seq[MenuWithNamePerDay]] = {
    def getMenuDetails(menuPerDay: MenuPerDay): Future[Option[MenuWithNamePerDay]] = {
      for {
        menu <- menuService.getByUuid(menuPerDay.menuUuid)
        result <- menu match {
          case Some(menuData) =>
            getNumberOfMenusPerDayPerPersonByMenuPerDay(menuPerDay.uuid).map(count =>
                Some(MenuWithNamePerDay(
                  menuPerDay.uuid,
                  menuData.uuid,
                  new SimpleDateFormat("dd-MM-yyyy").format(menuPerDay.date),
                  menuData.name,
                  numberOfPeopleSignedIn = count,
                  menuPerDay.location
                )))
          case None => Future.successful(None)
        }
      } yield result
    }

    for {
      menuPerDays <- menuPerDayService.getAllFutureAndOrderedByDate
      result <- Future.traverse(menuPerDays)(getMenuDetails).map(_.flatten)
    } yield result
  }

  def getAllMenuWithNamePerDayFilterDateRange(dateStart: java.sql.Date, dateEnd: java.sql.Date): Future[Seq[MenuWithNamePerDay]] = {
    def getMenuDetails(menuPerDay: MenuPerDay): Future[Option[MenuWithNamePerDay]] = {
      for {
        menu <- menuService.getByUuid(menuPerDay.menuUuid)
        result <- menu match {
          case Some(menuData) =>
            getAttendeeCountByMenuPerDayUuid(menuPerDay.uuid).map(count =>
                Some(MenuWithNamePerDay(
                  menuPerDay.uuid,
                  menuData.uuid,
                  new SimpleDateFormat("dd-MM-yyyy").format(menuPerDay.date),
                  menuData.name,
                  numberOfPeopleSignedIn = count,
                  menuPerDay.location
                )))
          case None => Future.successful(None)
        }
      } yield result
    }

    for {
      menuPerDays <- menuPerDayService.getAllOrderedByDateFilterDateRange(dateStart, dateEnd)
      result <- Future.traverse(menuPerDays)(getMenuDetails).map(_.flatten)
    } yield result
  }

  def deleteByMenuPerPersonUuid(menuPerDayUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.removeByMenuPerDayUuid(menuPerDayUuid)

  def getListOfPeopleByMenuPerDay(menuPerDayUuid: UUID): Future[Seq[MenuPerDayAttendant]] = {
    for {
      attendees <- MenuPerDayPerPersonTable.getAttendeesByMenuPerDayUuid(menuPerDayUuid)
    } yield attendees.map { case (user: User, userProfile: UserProfile) =>
      MenuPerDayAttendant(user.name, userProfile.otherRestriction.getOrElse(""))
    }
  }

  def getListOfPeopleByMenuPerDayForReport(menuPerDay: MenuPerDay): Future[Seq[MenuPerDayReport]] = {
    MenuPerDayPerPersonTable.getAttendeesByMenuPerDayUuid(menuPerDay.uuid).map(_.map(user => MenuPerDayReport(user._1.name, menuPerDay.date)))
  }

  def getListOfPeopleByMenuPerDayByLocationAndDateForReport(menuPerDay: MenuPerDay): Future[Seq[MenuPerDayReportByDateAndLocation]] = {
    MenuPerDayPerPersonTable.getAttendeesByMenuPerDayUuid(menuPerDay.uuid).map(_.map(user =>
            MenuPerDayReportByDateAndLocation(menuPerDay.date,
                                              menuPerDay.location,
                                              user._1.name))
    )
  }

  def getNotAttendingByDate(date: Date): Future[Seq[MenuPerDayReport]] = {
    MenuPerDayPerPersonTable .getNotAttendingByDate(date).map(_.map(user => MenuPerDayReport(user.name, date)))
  }

  private def isAttending(menuPerDayPerPerson: Option[MenuPerDayPerPerson]): Option[Boolean] = {
    menuPerDayPerPerson.map(_.isAttending)
  }

  def getAttendeesEmailAddressesForUpcomingLunch: Future[Seq[String]] = {
    MenuPerDayPerPersonTable.getAttendeesEmailAddressesForUpcomingLunch
  }

  private def getNumberOfMenusPerDayPerPersonByMenuPerDay(
      menuPerDayUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.getByMenuPerDayUuid(menuPerDayUuid).map(_.length)

  private def getAttendeeCountByMenuPerDayUuid(
      menuPerDayUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.getAttendeeCountByMenuPerDayUuid(menuPerDayUuid)

}
