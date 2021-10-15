package lunatech.lunchplanner.data

import java.util.UUID

import lunatech.lunchplanner.models.{ Dish, Menu, MenuWithDishes, MenuWithNamePerDay, User, UserProfile }
import play.api.libs.json.{ JsValue, Json }

object ControllersData {

  val createNewDishJson: JsValue = Json.parse(
    """
      |{
      |	"name": "new dish",
      |	"description": "new dish description",
      |	"isVegetarian": true,
      |	"hasSeaFood": false,
      |	"hasPork": false,
      |	"hasBeef": false,
      |	"hasChicken": false,
      |	"isGlutenFree": false,
      |	"hasLactose": false,
      |	"remarks": ""
      |}
    """.stripMargin)

  val createNewDish = Seq(
      "name" -> "new dish",
      "description" ->"new dish description",
      "isVegetarian" -> "true",
      "hasSeaFood" -> "false",
      "hasPork" -> "false",
      "hasBeef" -> "false",
      "hasChicken" -> "false",
      "isGlutenFree" -> "false",
      "hasLactose" -> "false",
      "remarks" -> "")

  val dish1 = Dish(UUID.randomUUID(), "Antipasto misto all italiana", "Selection of Italian cured meats & cheeses with assorted roasted vegetables in extra virgin olive oil", isVegetarian = false, isHalal = false, hasSeaFood = false, hasPork = false, hasBeef = true, hasChicken = false, isGlutenFree = false, hasLactose = false, None)
  val dish2 = Dish(UUID.randomUUID(), "Prosciutto crudo di Parma e melone", "Slices of melon draped with cured Italian ham", isVegetarian = false, isHalal = false, hasSeaFood = false, hasPork = false, hasBeef = true, hasChicken = false, isGlutenFree = false, hasLactose = false, None)
  val dish3 = Dish(UUID.randomUUID(), "Insalata tricolore", "Tomato, mozzarella, avocado & basil", isVegetarian = true, isHalal = false, hasSeaFood = false, hasPork = false, hasBeef = false, hasChicken = false, isGlutenFree = false, hasLactose = false, None)
  val dish4 = Dish(UUID.randomUUID(), "Avocado al forno", "Baked avocado topped with tomato sauce, mozzarella and touch of chilli", isVegetarian = true, isHalal = false, hasSeaFood = false, hasPork = false, hasBeef = false, hasChicken = false, isGlutenFree = false, hasLactose = false, None)
  val dish5 = Dish(UUID.randomUUID(), "Gamberoni all aglio", "King prawns panfried in garlic, olive oil, chilli & tomato", isVegetarian = false, isHalal = false, hasSeaFood = true, hasPork = false, hasBeef = false, hasChicken = false, isGlutenFree = false, hasLactose = false, None)


  val menu1 = Menu(name ="Menu 1")
  val menu2 = Menu(name ="Menu 2")

  val menuDish1 = MenuWithDishes(UUID.randomUUID(), menu1.name, Seq(dish2, dish3))
  val menuDish2 = MenuWithDishes(UUID.randomUUID(), menu2.name, Seq(dish1, dish5, dish4))

  val schedule1 = MenuWithNamePerDay(UUID.randomUUID(), menu1.uuid, "10-02-2017", menu1.name, 7, "Amsterdam")
  val schedule2 = MenuWithNamePerDay(UUID.randomUUID(), menu2.uuid, "05-04-2017", menu2.name, 9, "Rotterdam")

  val user1 = User(name = "user 1", emailAddress = "user1@lunatech.nl", isAdmin = true)
  val user2 = User(name = "user 2", emailAddress = "user2@lunatech.nl")
  val user3 = User(name = "user 3", emailAddress = "user3@lunatech.nl")
  val user4 = User(name = "user 4", emailAddress = "user4@lunatech.nl")
  val user5 = User(name = "user 5", emailAddress = "user5@lunatech.nl")

  val userProfile1 = UserProfile(vegetarian = true)
  val userProfile2 = UserProfile(glutenRestriction = true)
  val userProfile3 = UserProfile(lactoseRestriction = true)
  val userProfile4 = UserProfile(vegetarian = true)
}
