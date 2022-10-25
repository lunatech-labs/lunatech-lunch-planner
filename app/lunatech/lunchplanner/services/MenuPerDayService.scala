package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.MenuPerDay
import lunatech.lunchplanner.persistence.MenuPerDayTable

import java.sql.Date
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future

class MenuPerDayService @Inject() (menuService: MenuService)(implicit
    val connection: DBConnection
) {

  def add(menuPerDay: MenuPerDay): Future[MenuPerDay] =
    MenuPerDayTable.add(menuPerDay)

  def getAll: Future[Seq[MenuPerDay]] = MenuPerDayTable.getAll

  def getAllFutureAndOrderedByDate(
      limit: Option[Int] = None
  ): Future[Seq[MenuPerDay]] =
    MenuPerDayTable.getAllFutureAndOrderedByDateAscending(limit)

  def getAllOrderedByDateFilterDateRange(
      dateStart: Date,
      dateEnd: Date
  ): Future[Seq[MenuPerDay]] =
    MenuPerDayTable.getAllFilteredDateRangeOrderedDateAscending(
      dateStart,
      dateEnd
    )

  def getAllOrderedByDateFilterDateRangeWithDeleted(
      dateStart: Date,
      dateEnd: Date
  ): Future[Seq[MenuPerDay]] =
    MenuPerDayTable.getAllFilteredDateRangeOrderedDateAscendingWithDeleted(
      dateStart,
      dateEnd
    )

  def getMenuPerDayByUuid(uuid: UUID): Future[Option[MenuPerDay]] =
    MenuPerDayTable.getByUuid(uuid)

  def getAllByMenuUuid(menuUuid: UUID): Future[Seq[MenuPerDay]] =
    MenuPerDayTable.getByMenuUuid(menuUuid)

  def getMenuForUpcomingSchedule: Future[Seq[(MenuPerDay, String)]] =
    MenuPerDayTable.getMenuForUpcomingSchedule

  def update(uuid: UUID, menuPerDay: MenuPerDay): Future[Boolean] =
    MenuPerDayTable.update(menuPerDay.copy(uuid))

  def deleteByMenuUuid(menuUuid: UUID): Future[Int] =
    MenuPerDayTable.removeByMenuUuid(menuUuid)

  def delete(uuid: UUID): Future[Int] =
    MenuPerDayTable.removeByUuid(uuid)

  def getAllAvailableDatesWithinRange(
      dateStart: Date,
      dateEnd: Date
  ): Future[Seq[Date]] =
    MenuPerDayTable.getAllAvailableDatesWithinRange(dateStart, dateEnd)
}
