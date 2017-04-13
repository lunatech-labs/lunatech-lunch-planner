package lunatech.lunchplanner.services

import java.sql.Date
import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ MenuPerDay, MenuWithNamePerDay }
import lunatech.lunchplanner.persistence.MenuPerDayTable
import lunatech.lunchplanner.viewModels.MenuPerDayForm

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayService @Inject() (
  menuService: MenuService,
  implicit val connection: DBConnection) {

  def addNewMenuPerDay(menuPerDayForm: MenuPerDayForm): Future[MenuPerDay] = {
    val newMenuPerDay = MenuPerDay(menuUuid = menuPerDayForm.menuUuid, date = Date.valueOf(menuPerDayForm.date))
    MenuPerDayTable.addMenuPerDay(newMenuPerDay)
  }

  def getAllMenusPerDay: Future[Seq[MenuPerDay]] = MenuPerDayTable.getAllMenuPerDays

  def getAllMenusPerDayByMenuUuid(menuUuid: UUID): Future[Seq[MenuPerDay]] =
    MenuPerDayTable.getMenuPerDayByMenuUuid(menuUuid)

  def deleteMenuPerDayByMenuUuid(menuUuid: UUID): Future[Int] =
    MenuPerDayTable.removeMenuPerDayByMenuUuid(menuUuid)
}
