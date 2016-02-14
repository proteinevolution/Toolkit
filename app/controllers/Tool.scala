package controllers

import javax.inject.{Named, Singleton, Inject}


import actors.UserActor.{GetJob, PrepWD}
import actors.UserManager.GetUserActor
import akka.actor.ActorRef
import akka.util.Timeout
import models.tools.{Tcoffee, Alnviz}
import models.{Job, Session}
import play.api.Logger
import play.api.cache.CacheApi
import scala.concurrent.duration._
import play.api.i18n.{I18nSupport, MessagesApi}
import akka.pattern.ask
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by lukas on 1/27/16.
  */
@Singleton
class Tool @Inject()(val messagesApi: MessagesApi,
                     @Named("user-manager") userManager : ActorRef,
                     cache: CacheApi) extends Controller with I18nSupport {

  val UID = "uid"
  val toolname = "alnviz"

  implicit val timeout = Timeout(5.seconds)



  def show(toolname: String) = Action { implicit request =>

    Logger.info(s"{Tool} Input view for tool $toolname requested")


    // Determine view of tool
    // TODO Replace with reflection, otherwise we have to mention all tools explicitly here.
    val toolframe = toolname match {

      case "alnviz" => views.html.alnviz.form(Alnviz.inputForm)
      case "tcoffee" => views.html.tcoffee.form(Tcoffee.inputForm)
    }

    val view = views.html.general.submit(toolname, toolframe)

    Ok(views.html.main(view)).withSession {

      val uid = request.session.get(UID).getOrElse {

        Session.next.toString
      }
      Logger.info("Request from  UID" + uid)
      request.session + (UID -> uid)
    }
  }

  def submit(toolname: String) = Action.async { implicit request =>

    val uid = request.session.get(UID).get

    val jobID =  request.body.asFormUrlEncoded.get("jobid").head match {

      case m if m.isEmpty => None
      case m => Some(m)
    }

    (userManager ? GetUserActor(uid)).mapTo[ActorRef].map { userActor =>

      // TODO replace with reflection
      val form = toolname match {

        case "alnviz" => Alnviz.inputForm
        case "tcoffee" => Tcoffee.inputForm
      }
     form.bindFromRequest.fold(
        formWithErrors => {
          BadRequest("This was an error")
        },
        formdata => {
          Logger.info("{Tool} Form data sucessfully received")
          // TODO Determine whether user has submitted a jobid
          userActor ! PrepWD(toolname, getCCParams(formdata)  , true, jobID)
        }
      )
      Ok
    }
  }


  def result(jobID : String) = Action.async { implicit request =>

    val uid = request.session.get(UID).get

    (userManager ? GetUserActor(uid)).mapTo[ActorRef].flatMap { userActor =>
      (userActor ? GetJob(jobID)).mapTo[Job].map { job =>

       val toolframe = job.toolname match {

          case "alnviz" => views.html.alnviz.result(job.id, job)
          case "tcoffee" => views.html.tcoffee.result(job.id, job)
        }

        Ok(views.html.general.result(toolframe, job))
      }
    }
  }



  def getCCParams(cc: AnyRef) =
    (Map[String, Any]() /: cc.getClass.getDeclaredFields) {(a, f) =>
      f.setAccessible(true)
      a + (f.getName -> f.get(cc))
    }
}

