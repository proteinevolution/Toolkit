package controllers

import javax.inject.{Named, Singleton, Inject}


import actors.UserActor._
import actors.UserManager.GetUserActor
import akka.actor.ActorRef
import akka.util.Timeout
import models.tools._
import models.sessions.Session
import play.api.Logger
import scala.concurrent.duration._
import play.api.i18n.{I18nSupport, MessagesApi}
import akka.pattern.ask
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by lukas on 1/27/16.
  */
object Tool {

  val tools : List[ToolModel] = List(Hmmer3, Tcoffee, Alnviz, Psiblast) // list of all added tools


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
                     @Named("user-manager") userManager : ActorRef) extends Controller with I18nSupport {

  implicit val timeout = Timeout(5.seconds)


  def show(toolname: String) = Action { implicit request =>

    Redirect(s"/#/tools/$toolname")
  }


  def submit(toolname: String, startImmediate : Boolean, newSubmission : Boolean) = Action.async { implicit request =>

    val session_id = Session.requestSessionID(request) // Grab the Session ID

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

            // Decide whether to Prepare a new Job or alter the parameters of an already prepared one
            if(newSubmission) {
              userActor ! PrepWD(toolname, boundForm.data, startImmediate, job_id)

            } else {
              userActor ! UpdateWD(job_id.get, boundForm.data, startImmediate)
            }
          }
        )
        Ok
      }
    }
  }

  def result(job_id : String) = Action { implicit request =>

    Redirect(s"/#/jobs/$job_id")
 }
}
