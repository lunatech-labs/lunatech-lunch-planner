package lunatech.lunchplanner.data

import java.sql.Date

import lunatech.lunchplanner.models.{
  Dish,
  Menu,
  MenuDish,
  MenuPerDay,
  MenuPerDayPerPerson,
  User,
  UserProfile
}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

object TableDataGenerator {

  private val minDate = 0
  private val maxDate = 99999999999999L

  // replaceAll("\u0000", "") to avoid the error: 'invalid byte sequence for encoding "UTF8": 0x00'
  implicit val arbitraryUser: Arbitrary[User] = Arbitrary[User] {
    for {
      uuid         <- Gen.uuid
      name         <- arbitrary[String]
      emailAddress <- arbitrary[String]
      isAdmin      <- arbitrary[Boolean]
    } yield User(
      uuid = uuid,
      name = name.replaceAll("\u0000", ""),
      emailAddress = emailAddress.replaceAll("\u0000", ""),
      isAdmin = isAdmin
    )
  }

  implicit val arbitraryUserProfile: Arbitrary[UserProfile] =
    Arbitrary[UserProfile] {
      for {
        uuid               <- Gen.uuid
        vegetarian         <- arbitrary[Boolean]
        seaFoodRestriction <- arbitrary[Boolean]
        porkRestriction    <- arbitrary[Boolean]
        beefRestriction    <- arbitrary[Boolean]
        chickenRestriction <- arbitrary[Boolean]
        glutenRestriction  <- arbitrary[Boolean]
        lactoseRestriction <- arbitrary[Boolean]
        otherRestriction   <- arbitrary[Option[String]]
      } yield UserProfile(
        userUuid = uuid,
        vegetarian = vegetarian,
        seaFoodRestriction = seaFoodRestriction,
        porkRestriction = porkRestriction,
        beefRestriction = beefRestriction,
        chickenRestriction = chickenRestriction,
        glutenRestriction = glutenRestriction,
        lactoseRestriction = lactoseRestriction,
        otherRestriction = otherRestriction.map(_.replaceAll("\u0000", ""))
      )
    }

  // replaceAll("\u0000", "") to avoid the error: 'invalid byte sequence for encoding "UTF8": 0x00'
  implicit val arbitraryDish: Arbitrary[Dish] = Arbitrary[Dish] {
    for {
      uuid         <- Gen.uuid
      name         <- arbitrary[String]
      description  <- arbitrary[String]
      isVegetarian <- arbitrary[Boolean]
      hasSeaFood   <- arbitrary[Boolean]
      hasPork      <- arbitrary[Boolean]
      hasBeef      <- arbitrary[Boolean]
      hasChicken   <- arbitrary[Boolean]
      isGlutenFree <- arbitrary[Boolean]
      hasLactose   <- arbitrary[Boolean]
      remarks      <- arbitrary[Option[String]]
    } yield Dish(
      uuid = uuid,
      name = name.replaceAll("\u0000", ""),
      description = description.replaceAll("\u0000", ""),
      isVegetarian = isVegetarian,
      hasSeaFood = hasSeaFood,
      hasPork = hasPork,
      hasBeef = hasBeef,
      hasChicken = hasChicken,
      isGlutenFree = isGlutenFree,
      hasLactose = hasLactose,
      remarks = remarks.map(_.replaceAll("\u0000", ""))
    )
  }

  implicit val arbitraryMenuDish: Arbitrary[MenuDish] = Arbitrary[MenuDish] {
    for {
      uuid     <- Gen.uuid
      menuUuid <- Gen.uuid
      dishUuid <- Gen.uuid
    } yield MenuDish(
      uuid = uuid,
      menuUuid = menuUuid,
      dishUuid = dishUuid
    )
  }

  // replaceAll("\u0000", "") to avoid the error: 'invalid byte sequence for encoding "UTF8": 0x00'
  implicit val arbitraryMenu: Arbitrary[Menu] = Arbitrary[Menu] {
    for {
      uuid <- Gen.uuid
      name <- arbitrary[String]
    } yield Menu(
      uuid = uuid,
      name = name.replaceAll("\u0000", "")
    )
  }

  // replaceAll("\u0000", "") to avoid the error: 'invalid byte sequence for encoding "UTF8": 0x00'
  implicit val arbitraryMenuPerDay: Arbitrary[MenuPerDay] =
    Arbitrary[MenuPerDay] {
      for {
        uuid     <- Gen.uuid
        menuUuid <- Gen.uuid
        date     <- Gen.choose[Long](minDate, maxDate)
        location <- arbitrary[String]
      } yield MenuPerDay(
        uuid = uuid,
        menuUuid = menuUuid,
        date = new Date(date),
        location = location.replaceAll("\u0000", "")
      )
    }

  implicit val arbitraryMenuPerDayPerPerson: Arbitrary[MenuPerDayPerPerson] =
    Arbitrary[MenuPerDayPerPerson] {
      for {
        uuid           <- Gen.uuid
        menuPerDayUuid <- Gen.uuid
        userUuid       <- Gen.uuid
        isAttending    <- arbitrary[Boolean]
      } yield MenuPerDayPerPerson(
        uuid = uuid,
        menuPerDayUuid = menuPerDayUuid,
        userUuid = userUuid,
        isAttending = isAttending
      )
    }
}
