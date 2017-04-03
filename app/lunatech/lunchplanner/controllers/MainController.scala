package lunatech.lunchplanner.controllers

import javax.inject.Singleton
import play.api.mvc._

@Singleton
class MainController() extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

}
