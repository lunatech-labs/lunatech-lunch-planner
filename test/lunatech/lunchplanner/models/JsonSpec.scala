package lunatech.lunchplanner.models

import java.util.UUID

import lunatech.lunchplanner.data.ControllersData
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.Json

class JsonSpec extends AnyFlatSpec with Matchers {
  "User model" should "write it's JSON representation correctly" in {
    val uuid = UUID.randomUUID()
    val user = User(
      uuid = uuid,
      name = "user",
      emailAddress = "user@test.com",
      isAdmin = true
    )
    val expected = s"""
                  |{
                  |    "uuid":"${uuid.toString}",
                  |    "name":"user",
                  |    "emailAddress":"user@test.com",
                  |    "isAdmin":true,
                  |    "isDeleted":false
                  |}
                 """.stripMargin.stripTrailing().filterNot(_.isWhitespace)
    Json.toJson(user).toString() shouldBe expected
  }

  "Event model" should "write it's JSON representation correctly" in {
    val events = ControllersData.events
    val expected = s"""
                      |[
                      |    {
                      |        "menuUuid":"${ControllersData.event1MenuUuid.toString}",
                      |        "menuPerDayUuid":"${ControllersData.event1MenuPerDayUuid.toString}",
                      |        "name":"Lunch 1",
                      |        "date":${ControllersData.event1Epoch},
                      |        "location":"Rotterdam",
                      |        "attending":false,
                      |        "attendees":3,
                      |        "availableDishes": [
                      |            {
                      |                "uuid":"${ControllersData.dish1Uuid.toString}",
                      |                "name":"Bitterballen",
                      |                "description":"",
                      |                "isVegetarian":false,
                      |                "isHalal":false,
                      |                "hasSeaFood":false,
                      |                "hasPork":false,
                      |                "hasBeef":true,
                      |                "hasChicken":false,
                      |                "isGlutenFree":false,
                      |                "hasLactose":false,
                      |                "isDeleted":false
                      |            }
                      |        ]
                      |    }
                      |]
                 """.stripMargin.stripTrailing().filterNot(_.isWhitespace)
    Json.toJson(events).toString() shouldBe expected
  }
}
