package lunatech.lunchplanner.services

import lunatech.lunchplanner.common.PropertyTestingConfig
import org.scalacheck.Prop.forAll
import org.scalacheck.{ Gen, Properties }
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import wolfendale.scalacheck.regexp.RegexpGen
import lunatech.lunchplanner.models._

object UserServicePropertySpec extends Properties("UserService") with PropertyTestingConfig with MockitoSugar {
  private val configuration = mock[Configuration]
  private val userService = new UserService(configuration)

  private val emailAddressGen: Gen[String] = {
    for {
      email <- RegexpGen.from("^[a-zA-Z-]+[.][a-zA-Z-]+([.][a-zA-Z-]+)?@lunatech.nl")
    } yield email.normalize
  }

  property("extract user name from email") = forAll(emailAddressGen) { email =>
    createTestSchema()

    val name = userService.getUserNameFromEmail(email)

    dropTestSchema()

    name.split(" ").lengthCompare(2) >=0
    !name.contains('@')
  }
}

