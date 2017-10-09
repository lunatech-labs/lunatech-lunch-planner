package lunatech.lunchplanner.data

sealed trait Location {
  def name: String
}

object Location {
  def values: List[BaseLocation] = List(AMSTERDAM, ROTTERDAM)

  def forName(name: String): Option[BaseLocation] = values.find(_.name.toLowerCase == name.toLowerCase )

  case class BaseLocation(name: String) extends Location

  val AMSTERDAM = BaseLocation("Amsterdam")
  val ROTTERDAM = BaseLocation("Rotterdam")
}




