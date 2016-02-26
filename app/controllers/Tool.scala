package controllers

import javax.inject.{Named, Singleton, Inject}


import actors.UserActor.{GetAllJobs, GetJob, PrepWD}
import actors.UserManager.GetUserActor
import akka.actor.ActorRef
import akka.util.Timeout
import models.jobs.UserJob
import models.tools.{ToolModel, Hmmer3, Tcoffee, Alnviz}
import models.Session
import play.api.Logger
import play.api.libs.json.Json
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
        || tool.toolNameAbbreviation == toolName) return tool
    }
    return null
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


  val UID = "uid"
  val tools = ""

  implicit val timeout = Timeout(5.seconds)


  def jobs = Action.async { implicit request =>

    val user_id = request.session.get(UID).get.toLong

    (userManager ? GetUserActor(user_id)).mapTo[ActorRef].flatMap { userActor =>

      (userActor ? GetAllJobs).mapTo[Iterable[UserJob]].map { joblist =>

        val jobListObjs = for (job <- joblist) yield {
          Json.obj("t" -> job.toolname,
            "s" -> job.getState.no,
            "i" -> job.job_id)
        }
        Ok(Json.obj("jobs" -> jobListObjs))
      }
    }
  }

  def show2(toolname : String) = Action { implicit request =>
    Ok(views.html.tool.form(toolname))
  }

  def show(toolname: String) = Action { implicit request =>

    Logger.info(s"{Tool} Input view for tool $toolname requested")

    val tool = Tool.getToolModel(toolname)
    // Check if the tool name was ok.
    if (tool == null) NotFound
    else {
      // Determine view of tool
      //val inputForm = tool.inputForm
      // TODO Replace with reflection, otherwise we have to mention all tools explicitly here.
      val toolframe = tool.toolNameShort match {
        case "alnviz" => views.html.alnviz.form(Alnviz.inputForm)
        case "tcoffee" => views.html.tcoffee.form(Tcoffee.inputForm)
        case "hmmer3" => views.html.hmmer3.form(Hmmer3.inputForm)
      }

      val view = views.html.general.submit(tool.toolNameShort, toolframe)

      Ok(views.html.main(view, tool.toolNameLong)).withSession {

        val uid = request.session.get(UID).getOrElse {

          Session.next.toString
        }
        Logger.info("Request from  UID" + uid)
        request.session + (UID -> uid)
      }
    }
  }

  def submit(toolname: String) = Action.async { implicit request =>

    val user_id = request.session.get(UID).get.toLong

    val job_id =  request.body.asFormUrlEncoded.get("jobid").head match {

      case m if m.isEmpty => None
      case m => Some(m)
    }

    (userManager ? GetUserActor(user_id)).mapTo[ActorRef].map { userActor =>

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

            userActor ! PrepWD(toolname, boundForm.data, true, job_id) // The third argument is currently not used
          }
        )
        Ok
      }
    }
  }

  def result(main_id : String) = Action.async { implicit request =>

    val user_id = request.session.get(UID).get.toLong

    (userManager ? GetUserActor(user_id)).mapTo[ActorRef].flatMap { userActor =>
      (userActor ? GetJob(main_id)).mapTo[UserJob].map { job =>

        //TODO Calculate the appropriate visualizations of the tools
        val vis = Map("Simple" -> views.html.visualization.alignment.simple(s"/files/$main_id/sequences.clustalw_aln"),
                      "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/$main_id/sequences.clustalw_aln"))

        val toolframe = job.toolname match {

          case "alnviz" =>
            val vis = Map("BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/$main_id/result"))
            views.html.tool.visualizations(vis)

          case "tcoffee" => views.html.tool.visualizations(vis)
          case "hmmer3" => views.html.tool.visualizations(vis)
        }

        Ok(views.html.general.result(toolframe, job))
      }
    }
  }
}

