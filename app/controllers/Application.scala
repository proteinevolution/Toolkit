package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Bioinformatics Toolkit"))
  }

  def disclaimer = Action {
    Ok(views.html.disclaimer())
  }

  def contact = Action {
    Ok(views.html.contact())
  }

}
