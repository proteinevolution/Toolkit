package controllers

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import javax.inject.Inject

import scala.concurrent.Future


class Result @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def display(jobID: Long) = Action {

    Ok(views.html.results(jobID))
  }
}