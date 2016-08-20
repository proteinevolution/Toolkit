package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager.Prepare
import akka.actor.ActorRef
import akka.stream.Materializer
import akka.util.Timeout
import models.database.Session
import models.tools._

import scala.concurrent.duration._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}


/**
  * Created by lukas on 1/27/16.
  */
object Tool {

  // TODO Remove this
  val tools : List[ToolModel] = List(Hmmer3, Tcoffee, Alnviz, Psiblast, Mafft, Reformatb, Clans, HHpred, HHblits) // list of all added tools

}

@Singleton
class Tool @Inject()(val messagesApi: MessagesApi,
                     implicit val mat: Materializer,
                     @Named("jobManager") jobManager : ActorRef) extends Controller with I18nSupport with Session {

  implicit val timeout = Timeout(5.seconds)

  // submit file size is now restricted to 10 MB
  def submit(toolname: String, start : Boolean, jobID : Option[String]) = Action { implicit request =>

    // Fetch the job ID from the submission, might be the empty string
    //val jobID = request.body.asFormUrlEncoded.get("jobid").head --- There won't be a job ID in the request

    // TODO replace with reflection to avoid the need to mention each tool explicitly here
    val form = toolname match {
      case "alnviz" => Some(Alnviz.inputForm)
      case "tcoffee" => Some(Tcoffee.inputForm)
      case "hmmer3" => Some(Hmmer3.inputForm)
      case "hhpred" => Some(HHpred.inputForm)
      case "hhblits" => Some(HHblits.inputForm)
      case "psiblast" => Some(Psiblast.inputForm)
      case "mafft" => Some(Mafft.inputForm)
      case "reformatb" => Some(Reformatb.inputForm) // cluster version of reformat
      case "clans" => Some(Clans.inputForm)
      case _ => None
    }

    if (form.isEmpty)
      NotFound

    else {
      val boundForm = form.get.bindFromRequest

      boundForm.fold(
        formWithErrors => {

          BadRequest("There was an error with the Form")
        },
        _ => jobManager ! Prepare(getUser, jobID, toolname, boundForm.data, start = start)
      )
      Ok
    }
  }

}


