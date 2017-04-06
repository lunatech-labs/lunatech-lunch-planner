package lunatech.lunchplanner.services

import java.util.UUID
import javax.inject.Inject

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{ MenuPerDay, MenuPerDayPerPerson, MenuWithNamePerDayPerPerson }
import lunatech.lunchplanner.persistence.{ MenuPerDayPerPersonTable, MenuPerDayTable }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MenuPerDayPerPersonService  @Inject() (
  menuPerDayService: MenuPerDayService,
  implicit val connection: DBConnection) {

  def addNewMenusPerDayPerPerson(menuPerDayPerPerson: MenuPerDayPerPerson): Future[MenuPerDayPerPerson] =
    MenuPerDayPerPersonTable.addMenuPerDayPerPerson(menuPerDayPerPerson)

  def getAllMenusPerDayPerPersonByUserUuid(userUuid: UUID): Future[Seq[MenuPerDayPerPerson]] = MenuPerDayPerPersonTable.getMenuPerDayPerPersonByUserUuid(userUuid)

  def getAllMenuWithNamePerDayPerPerson(userUuid: UUID): Future[Seq[MenuWithNamePerDayPerPerson]]  = {
    val allMenusWithNamePerDay = menuPerDayService.getAllMenuWithNamePerDay

    allMenusWithNamePerDay.flatMap {
      Future.traverse(_) { menuWithNamePerDay =>
        val isMenuSelected = isMenuPerDaySelectedForPerson(userUuid, menuWithNamePerDay.uuid)
        isMenuSelected.map( isSelected =>
        MenuWithNamePerDayPerPerson(menuWithNamePerDay.uuid, menuWithNamePerDay.menuDateAndName, userUuid, isSelected))
      }
    }
  }

  def isMenuPerDaySelectedForPerson(userUuid: UUID, menuPerDayUuid: UUID): Future[Boolean] =
    MenuPerDayPerPersonTable.getMenuPerDayPerPersonByUserUuidAndMenuPerDayUuid(userUuid, menuPerDayUuid).map(_.isDefined)

  def removeMenuPerDayPerPerson(menuPerDayPerPersonUuid: UUID): Future[Int] =
    MenuPerDayPerPersonTable.removeMenuPerDayPerPerson(menuPerDayPerPersonUuid)

}
