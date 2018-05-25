package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models.{ Menu, MenuPerDay, MenuPerDayPerPerson, User, UserProfile }
import org.scalacheck._
import org.scalacheck.Prop._

import scala.concurrent.Await
import shapeless.contrib.scalacheck._

object UserProfileTableSpec extends Properties(name = "UserProfile") with PropertyTestingConfig {

  import lunatech.lunchplanner.data.TableDataGenerator._

  override def afterAll(): Unit = dbConnection.db.close()

  property("add a new user profile") = forAll { (user: User, userProfile: UserProfile) =>
    val result = addUserAndProfileToDB(user, userProfile)

    cleanUserAndProfileTable

    result
  }

  property("get user profile by user uuid") = forAll { (user: User, userProfile: UserProfile) =>
    addUserAndProfileToDB(user, userProfile)

    val result = Await.result(UserProfileTable.getByUserUUID(user.uuid), defaultTimeout).get

    cleanUserAndProfileTable

    result == userProfile.copy(userUuid = user.uuid)
  }

  property("get all user profiles") = forAll { (user: User, userProfile: UserProfile) =>
    addUserAndProfileToDB(user, userProfile)

    val result = Await.result(UserProfileTable.getAll, defaultTimeout)

    cleanUserAndProfileTable

    result == Seq(userProfile.copy(userUuid = user.uuid))
  }

  property("remove user profile") = forAll { (user: User, userProfile: UserProfile) =>
    addUserAndProfileToDB(user, userProfile)

    val result = Await.result(UserProfileTable.removeByUserUuid(user.uuid), defaultTimeout)
    val getByUuid = Await.result(UserProfileTable.getByUserUUID(user.uuid), defaultTimeout).get

    result == 1 && getByUuid.isDeleted
  }

  property("get summary of diet restrictions by menuPerDay") = forAll {
    (user: User, userProfile: UserProfile, menu: Menu, menuPerDay: MenuPerDay, menuPerDayPerPerson: MenuPerDayPerPerson) =>

      addUserAndProfileToDB(user, userProfile)
      Await.result(MenuTable.add(menu), defaultTimeout)
      Await.result(MenuPerDayTable.add(menuPerDay.copy(menuUuid = menu.uuid)), defaultTimeout)
      Await.result(MenuPerDayPerPersonTable.add(menuPerDayPerPerson.copy(menuPerDayUuid = menuPerDay.uuid, userUuid = user.uuid)), defaultTimeout)

      val result = Await.result(UserProfileTable.getRestrictionsByMenuPerDay(menuPerDay.uuid), defaultTimeout).head

      cleanDatabase

      result._1 == booleanToInt(userProfile.vegetarian) &&
      result._2 == booleanToInt(userProfile.seaFoodRestriction) &&
      result._3 == booleanToInt(userProfile.porkRestriction) &&
      result._4 == booleanToInt(userProfile.beefRestriction) &&
      result._5 == booleanToInt(userProfile.chickenRestriction) &&
      result._6 == booleanToInt(userProfile.glutenRestriction) &&
      result._7 == booleanToInt(userProfile.lactoseRestriction)
  }

  private def booleanToInt(boolean: Boolean): Int = if (boolean) 1 else 0

  private def addUserAndProfileToDB(user: User, userProfile: UserProfile): Boolean = {
    Await.result(UserTable.add(user), defaultTimeout)
    Await.result(UserProfileTable.insertOrUpdate(userProfile.copy(userUuid = user.uuid)), defaultTimeout)
  }
}
