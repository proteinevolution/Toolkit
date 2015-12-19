package controllers

import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Bioinformatics Toolkit"))
  }


  def disclaimer = Action {
    Ok(views.html.disclaimer())
  }

  def contact = Action {
    Ok(views.html.contact())
  }

  def footer = Action {
    Ok(views.html.contact())
  }

  def search = Action {

    Ok(views.html.search())
  }
}