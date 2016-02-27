package controllers

import javax.inject.{Inject, Singleton}

import actors.UserActor._
import actors.UserManager.GetUserActor
import akka.actor.ActorRef
import akka.util.Timeout
import jdk.internal.dynalink.linker.LinkerServices.Implementation
import models.jobs.{Prepared, Done, UserJob}
import models.tools.{ToolModel, Hmmer3, Tcoffee, Alnviz}
import models.Session
import play.api.Logger
import play.api.libs.json.Json
import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.i18n.{I18nSupport, MessagesApi}
import akka.pattern.ask
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject

/**
  * This controller is intended to provide a WebService Interface for Mithril
  *
  *
  * Created by lukas on 2/27/16.
  */

@Singleton
class Service @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport  {


  val UID = "uid"
  val tools = ""


  def newJob(toolname : String) = Action { implicit  request =>


    val toolframe = toolname match {
      case "alnviz" => views.html.alnviz.form(Alnviz.inputForm)
      case "tcoffee" => views.html.tcoffee.form(Tcoffee.inputForm)
      case "hmmer3" => views.html.hmmer3.form(Hmmer3.inputForm)
    }

    Ok(views.html.general.submit(toolname, toolframe)).withSession {

      val uid = request.session.get(UID).getOrElse {

        Session.next.toString
      }
      Logger.info("Request from  UID" + uid)
      request.session + (UID -> uid)
    }
  }
}
