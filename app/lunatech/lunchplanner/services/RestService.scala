package lunatech.lunchplanner.services

import java.sql.Date
import java.text.SimpleDateFormat
import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.time.{Instant, LocalDateTime, ZoneOffset}
import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import lunatech.lunchplanner.common.DBConnection
import lunatech.lunchplanner.models.{
  Dish,
  Event,
  Menu,
  MenuPerDay,
  MenuPerDayAttendant,
  MenuPerDayDietRestrictions,
  MenuWithNamePerDay,
  User,
  UserProfile
}

class RestService @Inject() (
    menuService: MenuService,
    menuPerDayService: MenuPerDayService,
    menuPerDayPerPersonService: MenuPerDayPerPersonService,
    menuDishService: MenuDishService
)(implicit val connection: DBConnection) {
  def getFutureEvents(user: User, limit: Int): Future[Seq[Event]] = {
    def getMenuDetails(
        menuPerDay: MenuPerDay
    ): Future[Option[Event]] =
      for {
        menu <- menuService.getByUuid(menuPerDay.menuUuid)
        dishes: Seq[Dish] <- menuDishService
          .getMenuListOfDishes(menuPerDay.menuUuid)
        attendees: Seq[(User, UserProfile)] <- menuPerDayPerPersonService
          .getUsersByMenuPerDay(menuPerDay.uuid)
      } yield menu.map { menu =>
        Event(
          menuUuid = menuPerDay.menuUuid,
          menuPerDayUuid = menuPerDay.uuid,
          name = menu.name,
          date = menuPerDay.date,
          location = menuPerDay.location,
          attending = attendees.exists { case (attendingUser, _) =>
            attendingUser.uuid == user.uuid
          },
          attendees = attendees.size,
          availableDishes = dishes
        )
      }

    for {
      menuPerDays: Seq[MenuPerDay] <- menuPerDayService
        .getAllFutureAndOrderedByDate(Some(limit))
      result: Seq[Event] <- Future
        .traverse(menuPerDays)(getMenuDetails)
        .map(_.flatten)
    } yield result
  }
}
