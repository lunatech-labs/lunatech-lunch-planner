package lunatech.lunchplanner.data

sealed trait Month {
  def month: String
}

object Month {

  case class BaseMonth(month: String) extends Month

  val JANUARY = BaseMonth("January")
  val FEBRUARY = BaseMonth("February")
  val MARCH = BaseMonth("March")
  val APRIL = BaseMonth("April")
  val MAY = BaseMonth("May")
  val JUNE = BaseMonth("June")
  val JULY = BaseMonth("July")
  val AUGUST = BaseMonth("August")
  val SEPTEMBER = BaseMonth("September")
  val OCTOBER = BaseMonth("October")
  val NOVEMBER = BaseMonth("November")
  val DECEMBER = BaseMonth("December")

  def values: List[BaseMonth] = List(JANUARY, FEBRUARY, MARCH, APRIL, MAY, JUNE, JULY, AUGUST, SEPTEMBER,
                                     OCTOBER, NOVEMBER, DECEMBER)
}
