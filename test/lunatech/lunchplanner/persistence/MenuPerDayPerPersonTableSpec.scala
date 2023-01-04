package lunatech.lunchplanner.persistence

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models._
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import org.scalacheck.Prop._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

object MenuPerDayPerPersonTableSpec
    extends Properties(name = "MenuPerDayPerPerson")
    with PropertyTestingConfig {

  import lunatech.lunchplanner.data.TableDataGenerator._

  property("add a new menu per day per person") = forAll {
    (
        user: User,
        menu: Menu,
        menuPerDay: MenuPerDay,
        menuPerDayPerPerson: MenuPerDayPerPerson
    ) =>
      createTestSchema()

      addUserAndMenuAndMenuPerDayToDB(user, menu, menuPerDay)

      val menuPerDayPerPersonToAdd = menuPerDayPerPerson.copy(
        menuPerDayUuid = menuPerDay.uuid,
        userUuid = user.uuid
      )
      val result = Await.result(
        MenuPerDayPerPersonTable.addOrUpdate(menuPerDayPerPersonToAdd),
        defaultTimeout
      )

      dropTestSchema()

      result == menuPerDayPerPersonToAdd
  }

  property("update a menu per day per person") = forAll {
    (
        user: User,
        menu: Menu,
        menuPerDay: MenuPerDay,
        menuPerDayPerPerson: MenuPerDayPerPerson
    ) =>
      createTestSchema()

      val menuPerDayPerPersonToAdd =
        addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result = Await
        .result(
          MenuPerDayPerPersonTable.addOrUpdate(
            menuPerDayPerPersonToAdd.copy(isAttending =
              !menuPerDayPerPerson.isAttending
            )
          ),
          defaultTimeout
        )

      dropTestSchema()

      result == menuPerDayPerPersonToAdd.copy(isAttending =
        !menuPerDayPerPerson.isAttending
      )
  }

  property("query for menus per day per person by uuid") = forAll {
    (
        user: User,
        menu: Menu,
        menuPerDay: MenuPerDay,
        menuPerDayPerPerson: MenuPerDayPerPerson
    ) =>
      createTestSchema()

      val menuPerDayPerPersonToAdd =
        addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result = Await
        .result(
          MenuPerDayPerPersonTable.getByUuid(menuPerDayPerPersonToAdd.uuid),
          defaultTimeout
        )
        .get

      dropTestSchema()

      result == menuPerDayPerPersonToAdd
  }

  property("query for menus per day per person by menu per day uuid") = forAll {
    (
        user: User,
        menu: Menu,
        menuPerDay: MenuPerDay,
        menuPerDayPerPerson: MenuPerDayPerPerson
    ) =>
      createTestSchema()

      val menuPerDayPerPersonAdded =
        addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result = Await.result(
        MenuPerDayPerPersonTable.getByMenuPerDayUuid(menuPerDay.uuid),
        defaultTimeout
      )

      dropTestSchema()

      result == Seq(menuPerDayPerPersonAdded)
  }

  property(
    "query for menus per day per person by non existing menu per day uuid"
  ) = forAll { menuPerDay: MenuPerDay =>
    createTestSchema()
    // skipped adding data to the DB

    val result = Await.result(
      MenuPerDayPerPersonTable.getByMenuPerDayUuid(menuPerDay.uuid),
      defaultTimeout
    )

    dropTestSchema()

    result == Seq.empty[MenuPerDayPerPerson]
  }

  property("query for menus per day per person by user uuid") = forAll {
    (
        user: User,
        menu: Menu,
        menuPerDay: MenuPerDay,
        menuPerDayPerPerson: MenuPerDayPerPerson
    ) =>
      createTestSchema()

      val menuPerDayPerPersonAdded =
        addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result = Await.result(
        MenuPerDayPerPersonTable.getByUserUuid(user.uuid),
        defaultTimeout
      )

      dropTestSchema()

      result == Seq(menuPerDayPerPersonAdded)
  }

  property(
    "query for menus per day per person by user uuid that does not exist in table"
  ) = forAll { user: User =>
    createTestSchema()
    // skipped adding data to the DB

    val result = Await.result(
      MenuPerDayPerPersonTable.getByUserUuid(user.uuid),
      defaultTimeout
    )

    dropTestSchema()

    result == Seq.empty[MenuPerDayPerPerson]
  }

  property("query for menus per day per person by uuid") = forAll {
    (
        user: User,
        menu: Menu,
        menuPerDay: MenuPerDay,
        menuPerDayPerPerson: MenuPerDayPerPerson
    ) =>
      createTestSchema()

      val menuPerDayPerPersonToAdd =
        addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result = Await.result(MenuPerDayPerPersonTable.getAll, defaultTimeout)

      dropTestSchema()

      result == Seq(menuPerDayPerPersonToAdd)
  }

  property(
    "query for menu per day per person by user uuid and menu per person uuid"
  ) = forAll {
    (
        user: User,
        menu: Menu,
        menuPerDay: MenuPerDay,
        menuPerDayPerPerson: MenuPerDayPerPerson
    ) =>
      createTestSchema()

      val menuPerDayPerPersonToAdd =
        addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result =
        Await
          .result(
            MenuPerDayPerPersonTable
              .getByUserUuidAndMenuPerDayUuid(user.uuid, menuPerDay.uuid),
            defaultTimeout
          )
          .get

      dropTestSchema()

      result == menuPerDayPerPersonToAdd
  }

  property("remove an existing menu per day per person by uuid") = forAll {
    (
        user: User,
        menu: Menu,
        menuPerDay: MenuPerDay,
        menuPerDayPerPerson: MenuPerDayPerPerson
    ) =>
      createTestSchema()

      val menuPerDayPerPersonToAdd =
        addUserAndMenuDataToDB(user, menu, menuPerDay, menuPerDayPerPerson)
      val result = Await.result(
        MenuPerDayPerPersonTable.removeByUuid(menuPerDayPerPersonToAdd.uuid),
        defaultTimeout
      )

      dropTestSchema()

      result == 1
  }

  property(
    "not fail when trying to remove a menu per day per person that does not exist"
  ) = forAll { menuPerDayPerPerson: MenuPerDayPerPerson =>
    createTestSchema()

    // skipped adding data to the DB

    val result = Await.result(
      MenuPerDayPerPersonTable.removeByUuid(menuPerDayPerPerson.uuid),
      defaultTimeout
    )

    dropTestSchema()

    result == 0
  }

  property("query the list of people by menu per day") = forAll {
    (
        user: User,
        userProfile: UserProfile,
        menu: Menu,
        menuPerDay: MenuPerDay,
        menuPerDayPerPerson: MenuPerDayPerPerson
    ) =>
      createTestSchema()

      addUserAndMenuDataToDB(
        user,
        menu,
        menuPerDay,
        menuPerDayPerPerson.copy(isAttending = true)
      )
      val userProfileToAdd = userProfile.copy(userUuid = user.uuid)
      Await.result(UserProfileTable.add(userProfileToAdd), defaultTimeout)

      val result = Await.result(
        MenuPerDayPerPersonTable.getAttendeesByMenuPerDayUuid(menuPerDay.uuid),
        defaultTimeout
      )

      dropTestSchema()

      result == Seq((user, userProfileToAdd))
  }

  private def addUserAndMenuAndMenuPerDayToDB(
      user: User,
      menu: Menu,
      menuPerDay: MenuPerDay
  ): MenuPerDay = {
    val query = for {
      _ <- UserTable.add(user)
      _ <- MenuTable.add(menu)
      addedMenuPerDay <- MenuPerDayTable.add(
        menuPerDay.copy(menuUuid = menu.uuid)
      )
    } yield addedMenuPerDay

    Await.result(query, defaultTimeout)
  }

  private def addUserAndMenuDataToDB(
      user: User,
      menu: Menu,
      menuPerDay: MenuPerDay,
      menuPerDayPerPerson: MenuPerDayPerPerson
  ): MenuPerDayPerPerson = {
    val query = for {
      _ <- UserTable.add(user)
      _ <- MenuTable.add(menu)
      addedMenuPerDay <- MenuPerDayTable.add(
        menuPerDay.copy(menuUuid = menu.uuid)
      )
      addedMenu <- MenuPerDayPerPersonTable.addOrUpdate(
        menuPerDayPerPerson
          .copy(menuPerDayUuid = addedMenuPerDay.uuid, userUuid = user.uuid)
      )
    } yield addedMenu

    Await.result(query, defaultTimeout)
  }
}
