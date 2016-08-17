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

  /** getToolModel
    * Returns the tool object for a tool's name, null when there is no such tool.
    *
    * @param toolName tool Name
    * @return
    */
  def getToolModel(toolName: String): ToolModel = {
    for (tool <- tools) {
      if ( tool.toolNameShort        == toolName
        || tool.toolNameLong         == toolName
        || tool.toolNameAbbreviation == toolName)  return tool
    }
    null
  }
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

    // Determine whether the toolname was fine
    val tool = Tool.getToolModel(toolname)

    // Check if the tool name was ok.
    if (tool == null)  NotFound
    else {

      // TODO replace with reflection to avoid the need to mention each tool explicitly here
      val form = tool.toolNameShort match {
        case "alnviz" => Alnviz.inputForm
        case "tcoffee" => Tcoffee.inputForm
        case "hmmer3" => Hmmer3.inputForm
        case "hhpred" => HHpred.inputForm
        case "hhblits" => HHblits.inputForm
        case "psiblast" => Psiblast.inputForm
        case "mafft" => Mafft.inputForm
        case "reformatb" => Reformatb.inputForm // cluster version of reformat
        case "clans" => Clans.inputForm
      }
      val boundForm = form.bindFromRequest

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


