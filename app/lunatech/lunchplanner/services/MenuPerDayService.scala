package lunatech.lunchplanner.services

import java.sql.Date
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.MenuPerDay
import lunatech.lunchplanner.persistence.MenuPerDayTable
import lunatech.lunchplanner.viewModels.MenuPerDayForm

import scala.concurrent.Future

class MenuPerDayService @Inject() (implicit val connection: DBConnection) {

  def addNewMenuPerDay(menuPerDayForm: MenuPerDayForm): Future[MenuPerDay] = {
    val newMenuPerDay = MenuPerDay(menuUuid = menuPerDayForm.menuUuid, date = Date.valueOf(menuPerDayForm.date))
    MenuPerDayTable.addMenuPerDay(newMenuPerDay)
  }
}
