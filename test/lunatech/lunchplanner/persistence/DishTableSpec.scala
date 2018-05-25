package lunatech.lunchplanner.persistence

import java.util.UUID

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models.Dish
import org.scalacheck._
import org.scalacheck.Prop._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import shapeless.contrib.scalacheck._

object DishTableSpec extends Properties(name = "Dish") with PropertyTestingConfig {

  import lunatech.lunchplanner.data.TableDataGenerator._

  override def afterAll(): Unit = dbConnection.db.close()

  property("add a new dish") = forAll { dish: Dish =>
    val result = addDishToDB(dish)

    cleanDishTable

    result == dish
  }

  property("query dishes by uuid") = forAll { dish: Dish =>
    addDishToDB(dish)
    val result = Await.result(DishTable.getByUuid(dish.uuid), defaultTimeout).get

    cleanDishTable

    result == dish
  }

  property("query all dishes") = forAll { (dish1: Dish, dish2: Dish) =>
    Await.result(
      for{
        _ <- DishTable.add(dish1)
        _ <- DishTable.add(dish2)
      } yield(),
      defaultTimeout)

    val result = Await.result(DishTable.getAll, defaultTimeout)

    cleanDishTable

    result.lengthCompare(2) == 0 &&
    result.exists(_.uuid == dish1.uuid) &&
    result.exists(_.uuid == dish2.uuid)
  }

  property("remove existing dishes by uuid") = forAll { dish: Dish =>
    addDishToDB(dish)
    val dishesRemoved = Await.result(DishTable.removeByUuid(dish.uuid), defaultTimeout)
    val getByUuis = Await.result(DishTable.getByUuid(dish.uuid), defaultTimeout).get

    cleanDishTable

    dishesRemoved == 1 && getByUuis.isDeleted
  }

  property("not fail when trying to remove a dish that does not exist") = forAll { dish: Dish =>
    val dishesRemoved = Await.result(DishTable.removeByUuid(UUID.randomUUID), defaultTimeout)

    cleanDishTable

    dishesRemoved == 0
  }

  property("update an existing dish by uuid") = forAll { dish: Dish =>
    addDishToDB(dish)
    val isDishUpdated = Await.result(DishTable.insertOrUpdate(dish.copy(description = "updated description")), defaultTimeout)

    assert(isDishUpdated)

    val updatedDish = Await.result(DishTable.getByUuid(dish.uuid), defaultTimeout).get

    cleanDishTable

    updatedDish.description == "updated description"
  }

  private def addDishToDB(dish: Dish): Dish = Await.result(DishTable.add(dish), defaultTimeout)
}
