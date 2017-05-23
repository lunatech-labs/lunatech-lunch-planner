package lunatech.lunchplanner.services

import java.sql.Date
import java.util.UUID

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{MenuPerDay, Report}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.mockito.Matchers.any
import org.mockito.Mockito._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class ReportServicesSpec extends PlaySpec with MockitoSugar {

  val menuPerDayPerPersonService = mock[MenuPerDayPerPersonService]
  val menuPerDayService = mock[MenuPerDayService]
  val connection = mock[DBConnection]
  val randomUUID = UUID.randomUUID()
  val defaultTimeout = 10.seconds

  when(menuPerDayService.getAllOrderedByDateFilterDateRange(any[Date], any[Date]))
    .thenReturn(Future(Seq(MenuPerDay(randomUUID, randomUUID, any[Date]))))
  when(menuPerDayPerPersonService.getListOfPeopleByMenuPerDay(randomUUID)).thenReturn(Future(Nil))

  val reportService = new ReportService(menuPerDayPerPersonService, menuPerDayService, connection)

  "report service" should {
    "report service generate report for controller" in {
      val report=Await.result(reportService.getReport(any[Date],any[Date]),defaultTimeout)
      report mustBe Report(Map.empty,0)
    }
  }
}
