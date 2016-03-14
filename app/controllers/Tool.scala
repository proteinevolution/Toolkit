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

  var tools:List[ToolModel] = List[ToolModel]()  // list of all added tools

  // TODO Get these imported from an external file
  tools::= Hmmer3
  tools::= Tcoffee
  tools::= Alnviz
  tools::= Psiblast


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


  /** addToolModel
    * adds a tool model to the controller
    *
    * @param toolModel
    */
  def addToolModel(toolModel:ToolModel) = {
    tools ::= toolModel
  }
}

@Singleton
class Tool @Inject()(val messagesApi: MessagesApi,
                     @Named("user-manager") userManager : ActorRef) extends Controller with I18nSupport {

  implicit val timeout = Timeout(5.seconds)


  def show(toolname: String) = Action { implicit request =>

    Redirect(s"/#/tools/$toolname")
  }


  def submit(toolname: String, startImmediate : Boolean) = Action.async { implicit request =>

    Logger.info("Start immediate was set to: " + startImmediate )

    val session_id = Session.requestSessionID(request) // Grab the Session ID

    val job_id =  request.body.asFormUrlEncoded.get("jobid").head match {

      case m if m.isEmpty => None
      case m => Some(m)
    }

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
        }
        val boundForm = form.bindFromRequest

        boundForm.fold(
          formWithErrors => {
            BadRequest("This was an error")
          },
          _ => {
            Logger.info("{Tool} Form data sucessfully received")
            Logger.info(boundForm.data.toString)

            userActor ! PrepWD(toolname, boundForm.data, startImmediate, job_id)
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
