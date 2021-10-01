package lunatech.lunchplanner.persistence

import java.sql.Date

import lunatech.lunchplanner.common.PropertyTestingConfig
import lunatech.lunchplanner.models.{Menu, MenuPerDay}
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

import scala.concurrent.Await

object MenuPerDayTableSpec
    extends Properties(name = "MenuPerDay")
    with PropertyTestingConfig {

  import lunatech.lunchplanner.data.TableDataGenerator._

  private val minDate = 0
  private val maxDate = 99999999999999L

  property("add a new menu per day") = forAll {
    (menu: Menu, menuPerDay: MenuPerDay) =>
      createTestSchema()

      Await.result(MenuTable.add(menu), defaultTimeout)

      val menuPerDayToAdd = menuPerDay.copy(menuUuid = menu.uuid)
      val result =
        Await.result(MenuPerDayTable.add(menuPerDayToAdd), defaultTimeout)

      dropTestSchema()

      result.date.toString == menuPerDayToAdd.date.toString &&
      result.location == menuPerDayToAdd.location &&
      result.menuUuid == menuPerDayToAdd.menuUuid &&
      result.uuid == menuPerDayToAdd.uuid
  }

  property("query for menus per day by uuid") = forAll {
    (menu: Menu, menuPerDay: MenuPerDay) =>
      createTestSchema()

      val menuPerDayToAdd = addMenuAndMenuPerDayToDB(menu, menuPerDay)

      val result = Await
        .result(MenuPerDayTable.getByUuid(menuPerDayToAdd.uuid), defaultTimeout)
        .get

      dropTestSchema()

      result.date.toString == menuPerDayToAdd.date.toString &&
      result.location == menuPerDayToAdd.location &&
      result.menuUuid == menuPerDayToAdd.menuUuid &&
      result.uuid == menuPerDayToAdd.uuid
  }

  property("query for menus per day by menu uuid") = forAll {
    (menu: Menu, menuPerDay: MenuPerDay) =>
      createTestSchema()

      val menuPerDayToAdd = addMenuAndMenuPerDayToDB(menu, menuPerDay)

      val result =
        Await.result(MenuPerDayTable.getByMenuUuid(menu.uuid), defaultTimeout)

      dropTestSchema()

      result.map(_.date.toString) == Seq(menuPerDayToAdd).map(_.date.toString)
  }

  property("query for menus per day by non existent menu uuid") = forAll {
    menuPerDay: MenuPerDay =>
      createTestSchema()

      // skipping adding menuPerDay to DB

      val result = Await.result(MenuPerDayTable.getByMenuUuid(menuPerDay.uuid),
                                defaultTimeout)

      dropTestSchema()

      result == Seq.empty[MenuPerDay]
  }

  property("query for menus per day by date") = forAll {
    (menu: Menu, menuPerDay: MenuPerDay) =>
      createTestSchema()

      val menuPerDayToAdd = addMenuAndMenuPerDayToDB(menu, menuPerDay)

      val result = Await.result(MenuPerDayTable.getByDate(menuPerDayToAdd.date),
                                defaultTimeout)

      dropTestSchema()

      result.map(_.date.toString) == Seq(menuPerDayToAdd).map(_.date.toString)
  }

  property("query for menus per day by date that does not exist in table") =
    forAll { menuPerDay: MenuPerDay =>
      createTestSchema()

      // skipping adding menuPerDay to DB

      val result =
        Await.result(MenuPerDayTable.getByDate(menuPerDay.date), defaultTimeout)

      dropTestSchema()

      result == Seq.empty[MenuPerDay]
    }

  property("query all menus per day") = forAll {
    (menu: Menu, menuPerDay1: MenuPerDay, menuPerDay2: MenuPerDay) =>
      createTestSchema()

      Await.result(MenuTable.add(menu), defaultTimeout)

      val menuPerDayToAdd1 = menuPerDay1.copy(menuUuid = menu.uuid)
      val menuPerDayToAdd2 = menuPerDay2.copy(menuUuid = menu.uuid)
      Await.result(MenuPerDayTable.add(menuPerDayToAdd1), defaultTimeout)
      Await.result(MenuPerDayTable.add(menuPerDayToAdd2), defaultTimeout)

      val result = Await.result(MenuPerDayTable.getAll, defaultTimeout)

      dropTestSchema()

      result.map(_.date.toString) == Seq(menuPerDayToAdd1, menuPerDayToAdd2)
        .map(_.date.toString)
  }

  property("query all menus per day ordered by date ascending") = forAll {
    (menu: Menu, menuPerDay1: MenuPerDay, menuPerDay2: MenuPerDay) =>
      createTestSchema()

      Await.result(MenuTable.add(menu), defaultTimeout)

      val menuPerDayToSmallerDate =
        menuPerDay1.copy(menuUuid = menu.uuid, date = new Date(minDate))
      val menuPerDayToAddBiggerDate =
        menuPerDay2.copy(menuUuid = menu.uuid, date = new Date(maxDate))
      Await.result(MenuPerDayTable.add(menuPerDayToSmallerDate), defaultTimeout)
      Await.result(MenuPerDayTable.add(menuPerDayToAddBiggerDate),
                   defaultTimeout)

      val result = Await.result(MenuPerDayTable.getAllOrderedByDateAscending,
                                defaultTimeout)

      dropTestSchema()

      result.map(_.date.toString) == Seq(
        menuPerDayToSmallerDate,
        menuPerDayToAddBiggerDate).map(_.date.toString)
  }

  property("query all future menus per day ordered by date ascending") =
    forAll { (menu: Menu, menuPerDay1: MenuPerDay, menuPerDay2: MenuPerDay) =>
      createTestSchema()

      Await.result(MenuTable.add(menu), defaultTimeout)

      val nearFuture = System.currentTimeMillis + 1
      val genFutureDate =
        Gen.choose[Long](nearFuture, maxDate).sample.getOrElse(maxDate)

      val menuPerDayToAddFuture =
        menuPerDay1.copy(menuUuid = menu.uuid, date = new Date(genFutureDate))
      val menuPerDayToAddPast =
        menuPerDay2.copy(menuUuid = menu.uuid, date = new Date(minDate))
      Await.result(MenuPerDayTable.add(menuPerDayToAddFuture), defaultTimeout)
      Await.result(MenuPerDayTable.add(menuPerDayToAddPast), defaultTimeout)

      val result =
        Await.result(MenuPerDayTable.getAllFutureAndOrderedByDateAscending,
                     defaultTimeout)

      dropTestSchema()

      result.map(_.date.toString) == Seq(menuPerDayToAddFuture).map(
        _.date.toString)
    }

  property("remove an existing menu per day by uuid") = forAll {
    (menu: Menu, menuPerDay: MenuPerDay) =>
      createTestSchema()

      val menuPerDayToAdd = addMenuAndMenuPerDayToDB(menu, menuPerDay)

      val result =
        Await.result(MenuPerDayTable.removeByUuid(menuPerDayToAdd.uuid),
                     defaultTimeout)
      val getByUuid = Await
        .result(MenuPerDayTable.getByUuid(menuPerDayToAdd.uuid), defaultTimeout)
        .get

      dropTestSchema()

      result == 1 && getByUuid.isDeleted
  }

  property(
    "not fail when trying to remove a menu per day that does not exist") =
    forAll { menuPerDay: MenuPerDay =>
      createTestSchema()

      // skipping adding menuPerDay to DB

      val result = Await.result(MenuPerDayTable.removeByUuid(menuPerDay.uuid),
                                defaultTimeout)

      dropTestSchema()

      result == 0
    }

  property("remove existing menu per day by menu uuid") = forAll {
    (menu: Menu, menuPerDay1: MenuPerDay, menuPerDay2: MenuPerDay) =>
      createTestSchema()

      Await.result(MenuTable.add(menu), defaultTimeout)

      val menuPerDayToAdd1 = menuPerDay1.copy(menuUuid = menu.uuid)
      val menuPerDayToAdd2 = menuPerDay2.copy(menuUuid = menu.uuid)
      Await.result(MenuPerDayTable.add(menuPerDayToAdd1), defaultTimeout)
      Await.result(MenuPerDayTable.add(menuPerDayToAdd2), defaultTimeout)

      val result = Await.result(MenuPerDayTable.removeByMenuUuid(menu.uuid),
                                defaultTimeout)

      dropTestSchema()

      result == 2
  }

  // In H2 this test does not work.
//  property("update an existing menu per day by uuid") = forAll { (menu: Menu, menuPerDay: MenuPerDay) =>
//    createTestSchema()
//
//    addMenuAndMenuPerDayToDB(menu, menuPerDay)
//
//    val newDate = Gen.choose[Long](minDate, maxDate).sample.getOrElse(maxDate)
//
//    val menuPerDayToAddUpdated = menuPerDay.copy(date = new Date(newDate))
//    Await.result(MenuPerDayTable.update(menuPerDayToAddUpdated), defaultTimeout)
//
//    val result = Await.result(MenuPerDayTable.getByUuid(menuPerDay.uuid), defaultTimeout).get
//
//    dropTestSchema()
//
//    result.date.toLocalDate == new Date(newDate).toLocalDate
//  }

  private def addMenuAndMenuPerDayToDB(menu: Menu,
                                       menuPerDay: MenuPerDay): MenuPerDay = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val menuPerDayToAdd = menuPerDay.copy(menuUuid = menu.uuid)
    val query = for {
      _ <- MenuTable.add(menu)
      addedMenuPerDay <- MenuPerDayTable.add(menuPerDayToAdd)
    } yield addedMenuPerDay

    Await.result(query, defaultTimeout)
  }
}
