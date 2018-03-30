package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models._
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import org.scalacheck.Prop._

import scala.concurrent.Await
import shapeless.contrib.scalacheck._

object MenuPerDayPerPersonTableSpec extends Properties(name = "MenuPerDayPerPerson") with PropertyTestingConfig {

  import lunatech.lunchplanner.data.TableDataGenerator._

  override def afterAll(): Unit = dbConnection.db.close()

  property("add a new menu per day per person") = forAll {
    (user: User, menu: Menu, menuPerDay: MenuPerDay, menuPerDayPerPerson: MenuPerDayPerPerson) =>

      addUserAndMenuAndMenuPerDayToDB(user, menu, menuPerDay)

      val menuPerDayPerPersonToAdd = menuPerDayPerPerson.copy(menuPerDayUuid = menuPerDay.uuid, userUuid = user.uuid)
      val result = Await.result(MenuPerDayPerPersonTable.add(menuPerDayPerPersonToAdd), defaultTimeout)

      cleanMenuPerDayPerPersonTable

      result == menuPerDayPerPersonToAdd
  }

  property("query for menus per day per person by uuid") = forAll {
    (user: User, menu: Menu, menuPerDay: MenuPerDay, menuPerDayPerPerson: MenuPerDayPerPerson) =>

      val menuPerDayPerPersonToAdd = addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result = Await.result(MenuPerDayPerPersonTable.getByUuid(menuPerDayPerPersonToAdd.uuid), defaultTimeout).get

      cleanMenuPerDayPerPersonTable

      result == menuPerDayPerPersonToAdd
  }

  property("query for menus per day per person by menu per day uuid") = forAll {
    (user: User, menu: Menu, menuPerDay: MenuPerDay, menuPerDayPerPerson: MenuPerDayPerPerson) =>

      val menuPerDayPerPersonAdded = addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result = Await.result(MenuPerDayPerPersonTable.getByMenuPerDayUuid(menuPerDay.uuid), defaultTimeout)

      cleanMenuPerDayPerPersonTable

      result == Seq(menuPerDayPerPersonAdded)
  }

  property("query for menus per day per person by non existing menu per day uuid") = forAll { menuPerDay: MenuPerDay =>
    // skipped adding data to the DB

    val result = Await.result(MenuPerDayPerPersonTable.getByMenuPerDayUuid(menuPerDay.uuid), defaultTimeout)

    cleanMenuPerDayPerPersonTable

    result == Seq.empty[MenuPerDayPerPerson]
  }

  property("query for menus per day per person by user uuid") = forAll {
    (user: User, menu: Menu, menuPerDay: MenuPerDay, menuPerDayPerPerson: MenuPerDayPerPerson) =>

      val menuPerDayPerPersonAdded = addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result = Await.result(MenuPerDayPerPersonTable.getByUserUuid(user.uuid), defaultTimeout)

      cleanMenuPerDayPerPersonTable

      result == Seq(menuPerDayPerPersonAdded)
  }

  property("query for menus per day per person by user uuid that does not exist in table") = forAll { (user: User) =>
    // skipped adding data to the DB

    val result = Await.result(MenuPerDayPerPersonTable.getByUserUuid(user.uuid), defaultTimeout)

    cleanMenuPerDayPerPersonTable

    result == Seq.empty[MenuPerDayPerPerson]
  }

  property("query for menus per day per person by uuid") = forAll {
    (user: User, menu: Menu, menuPerDay: MenuPerDay, menuPerDayPerPerson: MenuPerDayPerPerson) =>

      val menuPerDayPerPersonToAdd = addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result = Await.result(MenuPerDayPerPersonTable.getAll, defaultTimeout)

      cleanMenuPerDayPerPersonTable

      result == Seq(menuPerDayPerPersonToAdd)
  }

  property("query for menu per day per person by user uuid and menu per person uuid") = forAll {
    (user: User, menu: Menu, menuPerDay: MenuPerDay, menuPerDayPerPerson: MenuPerDayPerPerson) =>

      val menuPerDayPerPersonToAdd = addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result =
        Await.result(MenuPerDayPerPersonTable.getByUserUuidAndMenuPerDayUuid(user.uuid, menuPerDay.uuid), defaultTimeout)
          .get

      cleanMenuPerDayPerPersonTable

      result == menuPerDayPerPersonToAdd
  }

  property("remove an existing menu per day per person by uuid") = forAll {
    (user: User, menu: Menu, menuPerDay: MenuPerDay, menuPerDayPerPerson: MenuPerDayPerPerson) =>

      val menuPerDayPerPersonToAdd = addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result = Await.result(MenuPerDayPerPersonTable.removeByUuid(menuPerDayPerPersonToAdd.uuid), defaultTimeout)

      cleanMenuPerDayPerPersonTable

      result ==  1
  }

  property("not fail when trying to remove a menu per day per person that does not exist") = forAll {
    (menuPerDayPerPerson: MenuPerDayPerPerson) =>

      // skipped adding data to the DB

      val result = Await.result(MenuPerDayPerPersonTable.removeByUuid(menuPerDayPerPerson.uuid), defaultTimeout)

      result == 0
  }

  property("query the list of people by menu per day") = forAll {
    (user: User, userProfile: UserProfile, menu: Menu, menuPerDay: MenuPerDay, menuPerDayPerPerson: MenuPerDayPerPerson) =>

      addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson.copy(isAttending = true))
      val userProfileToAdd = userProfile.copy(userUuid = user.uuid)
      Await.result(UserProfileTable.insertOrUpdate(userProfileToAdd), defaultTimeout)

      val result = Await.result(MenuPerDayPerPersonTable.getAttendeesByMenuPerDayUuid(menuPerDay.uuid), defaultTimeout)

      cleanMenuPerDayPerPersonTable

      result == Seq((user, userProfileToAdd))
  }

  private def addUserAndMenuAndMenuPerDayToDB(user: User, menu: Menu, menuPerDay: MenuPerDay) = {
    Await.result(UserTable.add(user), defaultTimeout)
    Await.result(MenuTable.add(menu), defaultTimeout)
    Await.result(MenuPerDayTable.add(menuPerDay.copy(menuUuid = menu.uuid)), defaultTimeout)
  }

  private def addUserAndMenuDataToDB(user: User, menu: Menu, menuPerDay: MenuPerDay, menuPerDayPerPerson: MenuPerDayPerPerson) = {
    addUserAndMenuAndMenuPerDayToDB(user, menu, menuPerDay)

    val menuPerDayPerPersonToAdd = menuPerDayPerPerson.copy(menuPerDayUuid = menuPerDay.uuid, userUuid = user.uuid)
    Await.result(MenuPerDayPerPersonTable.add(menuPerDayPerPersonToAdd), defaultTimeout)
    menuPerDayPerPersonToAdd
  }
}
