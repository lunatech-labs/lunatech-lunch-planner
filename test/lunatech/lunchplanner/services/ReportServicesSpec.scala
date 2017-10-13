package lunatech.lunchplanner.services

import java.sql.Date
import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{MenuPerDay, Report}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ReportServicesSpec extends PlaySpec with MockitoSugar {

  val menuPerDayPerPersonService = mock[MenuPerDayPerPersonService]
  val menuPerDayService = mock[MenuPerDayService]
  implicit val connection = mock[DBConnection]
  val randomUUID = UUID.randomUUID()
  val defaultTimeout = 10.seconds

  when(menuPerDayService.getAllOrderedByDateFilterDateRange(any[Date], any[Date]))
    .thenReturn(Future(Seq(MenuPerDay(randomUUID, randomUUID, any[Date], "Rotterdam"))))
  when(menuPerDayPerPersonService.getListOfPeopleByMenuPerDay(randomUUID)).thenReturn(Future(Nil))
  when(menuPerDayPerPersonService.getListOfPeopleByMenuPerDayForReport(any[MenuPerDay])).thenReturn(Future(Nil))

  val reportService = new ReportService(menuPerDayPerPersonService, menuPerDayService, connection)

  "report service" should {
    "report service generate report for controller" in {
      val report=Await.result(reportService.getReport(1), defaultTimeout)
      report mustBe Report(Map.empty)
    }
  }
}
