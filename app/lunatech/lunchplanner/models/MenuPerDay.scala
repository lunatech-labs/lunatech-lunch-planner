package lunatech.lunchplanner.models

import java.sql.Date
import java.util.UUID

final case class MenuPerDay(
    uuid: UUID = UUID.randomUUID(),
    menuUuid: UUID,
    date: Date,
    location: String,
    isDeleted: Boolean = false
)

final case class MenuWithNamePerDay(
    uuid: UUID,
    menuUuid: UUID,
    menuDate: String,
    menuName: String,
    numberOfPeopleSignedIn: Int,
    location: String
)

final case class MenuPerDayDietRestrictions(
    menuPerDayUuid: UUID,
    vegetarian: Int = 0,
    seaFoodRestriction: Int = 0,
    porkRestriction: Int = 0,
    beefRestriction: Int = 0,
    chickenRestriction: Int = 0,
    glutenRestriction: Int = 0,
    lactoseRestriction: Int = 0
)

final case class MenuPerDayAttendant(name: String, otherRestrictions: String)

final case class MenuPerDayReport(name: String, date: Date)
final case class MenuPerDayReportByDateAndLocation(date: Date,
                                                   location: String,
                                                   attendeeName: String)
