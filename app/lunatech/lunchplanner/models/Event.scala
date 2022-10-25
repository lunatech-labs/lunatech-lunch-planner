package lunatech.lunchplanner.models

import java.sql.Date
import java.util.UUID

import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Writes}

/**
 * This object is a composite, similar to MenuPerDay but containing more related data
 * @param menuUuid Menu UUID
 * @param menuPerDayUuid MenuPerDay UUID
 * @param name Menu name
 * @param date MenuPerDay date
 * @param location MenuPerDay location
 * @param attending is the current user attending
 * @param attendees number of attendees
 * @param availableDishes available dishes
 */
case class Event(
    menuUuid: UUID,
    menuPerDayUuid: UUID,
    name: String,
    date: Date,
    location: String,
    attending: Boolean,
    attendees: Int,
    availableDishes: Seq[Dish]
)

object Event {
  implicit val writer: Writes[Event] =
    (JsPath \ "menuUuid")
      .write[UUID]
      .and((JsPath \ "menuPerDayUuid").write[UUID])
      .and((JsPath \ "name").write[String])
      .and((JsPath \ "date").write[Date])
      .and((JsPath \ "location").write[String])
      .and((JsPath \ "attending").write[Boolean])
      .and((JsPath \ "attendees").write[Int])
      .and((JsPath \ "availableDishes").write[Seq[Dish]])(unlift(Event.unapply))
}
