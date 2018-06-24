package controllers

import javax.inject.Inject
import play.api.mvc._

class DataController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def recentUpdates = Action {
    Ok(views.html.elements.recentupdates())
  }

}
