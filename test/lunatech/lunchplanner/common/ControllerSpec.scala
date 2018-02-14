package lunatech.lunchplanner.common

import org.scalatest.BeforeAndAfterAll
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Results
import play.api.test.Writeables

import scala.concurrent.duration._

trait ControllerSpec
  extends PlaySpec
    with Results
    with MockitoSugar
    with BeforeAndAfterAll
    with GuiceOneAppPerSuite
    with Writeables {

  val defaultTimeout: FiniteDuration = 10.seconds
}
