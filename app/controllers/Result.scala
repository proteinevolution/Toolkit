package controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import javax.inject.Inject


class Result @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def display(jobID: Long) = Action { implicit request =>

    Ok(views.html.results(jobID))
  }
}