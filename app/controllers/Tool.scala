package controllers

import javax.inject.{Named, Singleton, Inject}


import actors.UserActor._
import actors.UserManager.GetUserActor
import akka.actor.ActorRef
import akka.util.Timeout
import jdk.internal.dynalink.linker.LinkerServices.Implementation
import models.jobs.{Prepared, Done, UserJob}
import models.tools.{ToolModel, Hmmer3, Tcoffee, Alnviz}
import models.sessions.Session
import play.api.Logger
import play.api.libs.json.Json
import scala.concurrent.Future
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

  val tools = ""

  implicit val timeout = Timeout(5.seconds)


  def jobs = Action.async { implicit request =>

    val session_id = Session.requestSessionID(request) // Grab the Session ID

    (userManager ? GetUserActor(session_id)).mapTo[ActorRef].flatMap { userActor =>

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

        val session_id = Session.requestSessionID(request) // Grab the Session ID
        Session.closeSessionRequest(request, session_id)   // Send Session Cookie
      }
    }
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

  def result(job_id : String) = Action.async { implicit request =>

    val session_id = Session.requestSessionID(request) // Grab the Session ID

    (userManager ? GetUserActor(session_id)).mapTo[ActorRef].flatMap { userActor =>
      (userActor ? GetJob(job_id)).mapTo[UserJob].flatMap { job =>


        // Switch on Job state to decide what to show
        job.getState match {


          case Done => Future {

            // TODO Dynamically calculate appropriate visualizations
            val vis = Map("Simple" -> views.html.visualization.alignment.simple(s"/files/$job_id/sequences.clustalw_aln"),
              "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/$job_id/sequences.clustalw_aln"))

            val toolframe = job.toolname match {

              case "alnviz" =>
                val vis = Map("BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/$job_id/result"))
                views.html.tool.visualizations(vis, job)

              case "tcoffee" => views.html.tool.visualizations(vis, job)
              case "hmmer3" => views.html.tool.visualizations(vis, job)
            }

            Ok(views.html.general.result(toolframe, job))
        }
          case Prepared =>
            Logger.info("Prepared job requested")

            (userActor ? GetJobParams(job.job_id)).mapTo[Map[String, String]].map { res =>


              val toolframe = job.toolname match {
                case "alnviz" => views.html.alnviz.form(Alnviz.inputForm.bind(res))
                case "tcoffee" => views.html.tcoffee.form(Tcoffee.inputForm.bind(res))
                case "hmmer3" => views.html.hmmer3.form(Hmmer3.inputForm.bind(res))
              }


              Ok(views.html.general.submit(job.toolname, toolframe)).withSession {
                Session.closeSessionRequest(request, session_id)   // Send Session Cookie
              }
        }
        }
      }
      }
 }
}

