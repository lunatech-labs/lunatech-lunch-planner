package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.{ AcceptanceSpec, DBConnection, TestDatabaseProvider }
import lunatech.lunchplanner.models.Dish

import scala.concurrent.Await
import scala.concurrent.duration._

class DishTableSpec extends AcceptanceSpec with TestDatabaseProvider {
  implicit private val dbConnection = app.injector.instanceOf[DBConnection]
  private val defaultTimeout = 10.seconds

  private val pastaBologneseDish = Dish(
    name = "pasta bolognese",
    description = "pasta bolognese for 2 people",
    hasBeef = true,
    remarks = Some("favorite dish of person A")
  )

  private val vegetarianDish = Dish(
    name = "vegetarian",
    description = "warm vegetarian food from fancy restaurant",
    isVegetarian = true,
    isGlutenFree = true,
    hasLactose = true
  )

  private val seaFoodDish = Dish(
    name = "shrimps",
    description = "fresh shrimps",
    hasSeaFood = true
  )

  private val porkDish = Dish(
    name = "pork",
    description = "nice dish with pork",
    hasPork = true
  )

  private val chickenDish = Dish(
    name = "chicken sate",
    description = "suriname sate dish",
    hasChicken = true
  )

  override def beforeAll {
    cleanDatabase()
    Await.result(DishTable.add(vegetarianDish), defaultTimeout)
    Await.result(DishTable.add(seaFoodDish), defaultTimeout)
    Await.result(DishTable.add(porkDish), defaultTimeout)
    Await.result(DishTable.add(chickenDish), defaultTimeout)
  }

  "A Dish table" must {
    "add a new dish" in {
      val result = Await.result(DishTable.add(pastaBologneseDish), defaultTimeout)
      result mustBe pastaBologneseDish
    }

    "query for existing dishes successfully" in {
      val result = Await.result(DishTable.exists(pastaBologneseDish.uuid), defaultTimeout)
      result mustBe true
    }

    "query for dishes by uuid" in {
      val result = Await.result(DishTable.getByUUID(vegetarianDish.uuid), defaultTimeout)
      result mustBe Some(vegetarianDish)
    }

    "query for dishes by name" in {
      val result = Await.result(DishTable.getByName(vegetarianDish.name), defaultTimeout)
      result mustBe Some(vegetarianDish)
    }

    "query all vegetarian dishes" in {
      val result = Await.result(DishTable.getAllVegetarianDishes, defaultTimeout)
      result mustBe Vector(vegetarianDish)
    }

    "query all seafood dishes" in {
      val result = Await.result(DishTable.getAllSeaFoodDishes, defaultTimeout)
      result mustBe Vector(seaFoodDish)
    }

    "query all pork dishes" in {
      val result = Await.result(DishTable.getAllPorkDishes, defaultTimeout)
      result mustBe Vector(porkDish)
    }

    "query all beef dishes" in {
      val result = Await.result(DishTable.getAllBeefDishes, defaultTimeout)
      result mustBe Vector(pastaBologneseDish)
    }

    "query all chicken dishes" in {
      val result = Await.result(DishTable.getAllChickenDishes, defaultTimeout)
      result mustBe Vector(chickenDish)
    }

    "query all gluten free dishes" in {
      val result = Await.result(DishTable.getAllGlutenFreeDishes, defaultTimeout)
      result mustBe Vector(vegetarianDish)
    }

    "query all lactose dishes" in {
      val result = Await.result(DishTable.getAllLactoseDishes, defaultTimeout)
      result mustBe Vector(vegetarianDish)
    }

    "query all dishes" in {
      val result = Await.result(DishTable.getAll, defaultTimeout)
      result mustBe Vector(vegetarianDish, seaFoodDish, porkDish, chickenDish, pastaBologneseDish)
    }

    "remove an existing dish by uuid" in {
      val result = Await.result(DishTable.remove(vegetarianDish.uuid), defaultTimeout)
      result mustBe 1
    }

    "not fail when trying to remove a dish that does not exist" in {
      val result = Await.result(DishTable.remove(UUID.randomUUID()), defaultTimeout)
      result mustBe 0
    }

    "update an existing dish by uuid" in {
      val updatedPastaBologneseDish = pastaBologneseDish.copy(
        description = "updated description")

      val result = Await.result(DishTable.insertOrUpdate(updatedPastaBologneseDish), defaultTimeout)
      result mustBe true

      val updatedDish = Await.result(DishTable.getByUUID(updatedPastaBologneseDish.uuid), defaultTimeout)
      updatedDish.get.description mustBe "updated description"
    }
  }
}
