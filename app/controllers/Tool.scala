package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager.Prepare
import akka.actor.ActorRef
import akka.util.Timeout
import models.tools._
import models.sessions.Session

import scala.concurrent.duration._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.Logger

/**
  * Created by lukas on 1/27/16.
  */
object Tool {

  val tools : List[ToolModel] = List(Hmmer3, Tcoffee, Alnviz, Psiblast, Mafft, Reformatb) // list of all added tools


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
                     @Named("jobManager") jobManager : ActorRef) extends Controller with I18nSupport {

  implicit val timeout = Timeout(5.seconds)


  def submit(toolname: String, start : Boolean, jobID : Option[Int]) = Action { implicit request =>


    val sessionID = Session.requestSessionID(request) // Grab the Session ID

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
        case "psiblast" => Psiblast.inputForm
        case "mafft" => Mafft.inputForm
        case "reformatb" => Reformatb.inputForm // cluster version of reformat
      }
      val boundForm = form.bindFromRequest

      boundForm.fold(
        formWithErrors => {

          BadRequest("There was an error with the Form")
        },
        _ => jobManager ! Prepare(sessionID, jobID, toolname, boundForm.data, start = start)
      )
      Ok
    }
  }
}

/*
case class Prepare(sessionID : String,
                     jobID : String,
                     toolname : String,
                     params : Map[String, String],
                     newJob : Boolean) extends UserRequestWithJob(sessionID, jobID)

 */


/*
// Determine the submitted JobID
    val job_id =  request.body.asFormUrlEncoded.get("jobid").head match {

      // If the user has not provided any, this will be None
      case m if m.isEmpty => None

      // If the user has provided one or the Job is an already exisiting one, then there is a Job ID
      case m => Some(m)
    }

    Logger.info("Submission for JobID " + job_id.toString + " received")

    (userManager ? GetUserActor(session_id)).mapTo[ActorRef].map { userActor =>

      val tool = Tool.getToolModel(toolname)
      // Check if the tool name was ok.
      if (tool == null)
        NotFound
      else {
        //val form = tool.inputForm
        // TODO replace with reflection
        val form = tool.toolNameShort match {
          case "alnviz" => Alnviz.inputForm
          case "tcoffee" => Tcoffee.inputForm
          case "hmmer3" => Hmmer3.inputForm
          case "psiblast" => Psiblast.inputForm
          case "reformat" => Reformat.inputForm
        }
        val boundForm = form.bindFromRequest

        boundForm.fold(
          formWithErrors => {
            BadRequest("This was an error")
          },
          _ => {

            userActor ! PrepWD(toolname, boundForm.data, startImmediate, job_id, newSubmission)
          }
        )
        Ok
      }
    }


 */
