package lunatech.lunchplanner.services

import java.sql.Date
import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.MenuPerDay
import lunatech.lunchplanner.persistence.MenuPerDayTable
import lunatech.lunchplanner.viewModels.MenuPerDayForm

import scala.concurrent.Future

class MenuPerDayService @Inject() (
  menuService: MenuService,
  implicit val connection: DBConnection) {

  def add(menuPerDayForm: MenuPerDayForm): Future[MenuPerDay] = {
    val newMenuPerDay = MenuPerDay(menuUuid = menuPerDayForm.menuUuid, date = new Date(menuPerDayForm.date.getTime))
    MenuPerDayTable.add(newMenuPerDay)
  }

  def getAll: Future[Seq[MenuPerDay]] = MenuPerDayTable.getAll

  def getAllOrderedByDateAscending: Future[Seq[MenuPerDay]] = MenuPerDayTable.getAllFutureAndOrderedByDateAscending

  def getMenuPerDayByUuid(uuid: UUID): Future[Option[MenuPerDay]] = MenuPerDayTable.getByUUID(uuid)

  def getAllByMenuUuid(menuUuid: UUID): Future[Seq[MenuPerDay]] =
    MenuPerDayTable.getByMenuUuid(menuUuid)

  def insertOrUpdate(uuid: UUID, menuPerDayForm: MenuPerDayForm): Future[Boolean] = {
    val menuPerDay = MenuPerDay(uuid, menuPerDayForm.menuUuid, new Date(menuPerDayForm.date.getTime))
    MenuPerDayTable.insertOrUpdate(menuPerDay)
  }

  def deleteByMenuUuid(menuUuid: UUID): Future[Int] =
    MenuPerDayTable.removeByMenuUuid(menuUuid)

  def delete(uuid: UUID): Future[Int] =
    MenuPerDayTable.remove(uuid)
}
