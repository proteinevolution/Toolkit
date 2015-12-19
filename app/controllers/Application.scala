package controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import javax.inject.Inject

class Application @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

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