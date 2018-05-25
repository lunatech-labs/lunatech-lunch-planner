package lunatech.lunchplanner.services

import java.sql.Date
import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.MenuPerDay
import lunatech.lunchplanner.persistence.MenuPerDayTable

import scala.concurrent.Future

class MenuPerDayService @Inject()(menuService: MenuService)(
    implicit val connection: DBConnection) {

  def add(menuPerDay: MenuPerDay): Future[MenuPerDay] = {
    MenuPerDayTable.add(menuPerDay)
  }

  def getAll: Future[Seq[MenuPerDay]] = MenuPerDayTable.getAll

  def getAllFutureAndOrderedByDate: Future[Seq[MenuPerDay]] =
    MenuPerDayTable.getAllFutureAndOrderedByDateAscending

  def getAllOrderedByDateFilterDateRange(
      dateStart: Date,
      dateEnd: Date): Future[Seq[MenuPerDay]] =
    MenuPerDayTable.getAllFilteredDateRangeOrderedDateAscending(dateStart,
                                                                dateEnd)

  def getAllOrderedByDateFilterDateRangeWithDeleted(
    dateStart: Date,
    dateEnd: Date): Future[Seq[MenuPerDay]] =
    MenuPerDayTable.getAllFilteredDateRangeOrderedDateAscendingWithDeleted(dateStart,
      dateEnd)

  def getMenuPerDayByUuid(uuid: UUID): Future[Option[MenuPerDay]] =
    MenuPerDayTable.getByUuid(uuid)

  def getAllByMenuUuid(menuUuid: UUID): Future[Seq[MenuPerDay]] =
    MenuPerDayTable.getByMenuUuid(menuUuid)

  def getMenuForUpcomingSchedule: Future[Seq[(MenuPerDay, String)]] = {
    MenuPerDayTable.getMenuForUpcomingSchedule
  }

  def insertOrUpdate(uuid: UUID, menuPerDay: MenuPerDay): Future[Boolean] = {
    MenuPerDayTable.insertOrUpdate(menuPerDay.copy(uuid))
  }

  def deleteByMenuUuid(menuUuid: UUID): Future[Int] =
    MenuPerDayTable.removeByMenuUuid(menuUuid)

  def delete(uuid: UUID): Future[Int] =
    MenuPerDayTable.removeByUuid(uuid)

  def getAllAvailableDatesWithinRange(dateStart: Date,
                                      dateEnd: Date): Future[Seq[Date]] = {
    MenuPerDayTable.getAllAvailableDatesWithinRange(dateStart, dateEnd)
  }
}
