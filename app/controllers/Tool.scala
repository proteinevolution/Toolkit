package controllers

import javax.inject.{Named, Singleton, Inject}


import actors.UserActor.{GetJob, PrepWD}
import actors.UserManager.GetUserActor
import akka.actor.ActorRef
import akka.util.Timeout
import models.jobs.{UserJob, UserJob$}
import models.tools.{Tcoffee, Alnviz}
import models.Session
import play.api.Logger
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
                     @Named("user-manager") userManager : ActorRef) extends Controller with I18nSupport {


  val UID = "uid"

  implicit val timeout = Timeout(5.seconds)

  def show(toolname: String) = Action { implicit request =>

    Logger.info(s"{Tool} Input view for tool $toolname requested")


    // Determine view of tool
    // TODO Replace with reflection, otherwise we have to mention all tools explicitly here.
    val toolframe = toolname match {

      case "alnviz" => views.html.alnviz.form(Alnviz.inputForm)
      case "tcoffee" => views.html.tcoffee.form(Tcoffee.inputForm)
    }
    val toolcompletename = toolname match {

      case "alnviz" => models.tools.Alnviz.fullName
      case "tcoffee" => models.tools.Tcoffee.fullName
    }

    val view = views.html.general.submit(toolname, toolframe)

    Ok(views.html.main(view, toolcompletename)).withSession {

      val uid = request.session.get(UID).getOrElse {

        Session.next.toString
      }
      Logger.info("Request from  UID" + uid)
      request.session + (UID -> uid)
    }
  }

  def submit(toolname: String) = Action.async { implicit request =>

    val user_id = request.session.get(UID).get.toLong

    val job_id =  request.body.asFormUrlEncoded.get("jobid").head match {

      case m if m.isEmpty => None
      case m => Some(m)
    }

    (userManager ? GetUserActor(user_id)).mapTo[ActorRef].map { userActor =>

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

          userActor ! PrepWD(toolname, getCCParams(formdata)  , true, job_id)
        }
      )
      Ok
    }
  }


  def result(main_id : String) = Action.async { implicit request =>

    val user_id = request.session.get(UID).get.toLong

    (userManager ? GetUserActor(user_id)).mapTo[ActorRef].flatMap { userActor =>
      (userActor ? GetJob(main_id)).mapTo[UserJob].map { job =>

        //TODO Calculate the appropriate visualizations of the tools
        val vis = Map("Simple" -> views.html.visualization.alignment.simple(s"/files/$main_id/sequences.clustalw_aln"))

        val toolframe = job.toolname match {

          case "alnviz" => views.html.alnviz.result(job)
          case "tcoffee" => views.html.tool.visualizations(vis)
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

