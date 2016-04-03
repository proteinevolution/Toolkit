package controllers

import javax.inject.{Inject, Singleton}

import actors.MasterConnection
import akka.util.Timeout
import models.distributed.FrontendMasterProtocol.{Delete, Get, JobUnknown, SessionIDUnknown}
import models.graph.Link
import models.jobs._
import models.sessions.Session
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

/**
  * This controller is intended to provide a WebService Interface for Mithril
  *
  *
  * Created by lukas on 2/27/16.
  */

@Singleton
class Service @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {

  implicit val timeout = Timeout(5.seconds)


  // Defines which messages the user can pass to the server
  case class AddChildJob(parent_job_id: String, toolname: String, links: Seq[Link])


  /*
    Defines appropriate JSON conversions
   */
  implicit val readsLink: Reads[Link] = (
    (JsPath \ "out").read[Int](min(0)) and
      (JsPath \ "in").read[Int](min(0))
    ) (Link.apply _)

  implicit val readsAddChildJob: Reads[AddChildJob] = (
    (JsPath \ "parent_job_id").read[String] and
      (JsPath \ "toolname").read[String] and
      (JsPath \ "links").read[Seq[Link]]
    ) (AddChildJob.apply _)

  /**
    * User asks to delete the Job with the provided job_id
    *
    * @param jobID
    * @return
    */
  def delJob(jobID: String) = Action { implicit request =>

    val sessionID = Session.requestSessionID(request) // Grab the Session ID
    MasterConnection.master ! Delete(sessionID, jobID)

    Ok
  }

  /*
  val addChild = Action.async(parse.json) { implicit request =>

    Logger.info("Add child Job received")

    val session_id = Session.requestSessionID(request) // Grab the Session ID

    (userManager ? GetUserActor(session_id)).mapTo[ActorRef].map { userActor =>

      request.body.validate[AddChildJob] match {

        case JsSuccess(addChildJob, _) =>

          userActor ! AppendChildJob(addChildJob.parent_job_id, addChildJob.toolname, addChildJob.links)
          Logger.info("JSON Data seems to be fine")
          Ok

        case JsError(errors) =>
          Logger.info("JSON Data  has errors")
          BadRequest
      }
    }
  } */






  def getJob(jobID: String) = Action.async { implicit request =>

    val sessionID = Session.requestSessionID(request) // Grab the Session ID

    (MasterConnection.master ? Get(sessionID, jobID)).map {

      case SessionIDUnknown => NotFound
      case JobUnknown => NotFound

      case userJob : UserJob =>

        // Decide what to show depending on the JobState

      userJob.getState match {

        // User has requested a job whose state is Running
        case Running => Ok(views.html.job.running(userJob)).withSession {
          Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
        }
        // User requested job whose execution is done
        case Done =>

          val toolframe = userJob.tool.toolname match {

            //  The tool anlviz just returns the BioJS MSA Viewer page
            case "alnviz" =>
              val vis = Map("BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/$jobID/result"))
              views.html.job.result(vis, userJob)


            // For T-Coffee, we provide a simple alignment visualiation and the BioJS View
            case "tcoffee" =>

              val vis = Map(
                "Simple" -> views.html.visualization.alignment.simple(s"/files/$jobID/sequences.clustalw_aln"),
                "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/$jobID/sequences.clustalw_aln"))

              views.html.job.result(vis, userJob)

            // Hmmer just provides a simple file viewer.
            case "hmmer3" => views.html.visualization.general.fileview(
              Array(s"/files/$jobID/domtbl", s"/files/$jobID/outfile", s"/files/$jobID/outfile_multi_sto", s"/files/$jobID/tbl"))
          }
          Ok(toolframe).withSession {
            Session.closeSessionRequest(request, sessionID, userJob.mainID)   // Send Session Cookie
          }

        case Error =>

          Ok(views.html.job.error(userJob)).withSession {
            Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
          }

        case Queued =>

          Ok(views.html.job.queued(userJob)).withSession {
            Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
          }



      }
    }
  }
}


  /*
  def getJob(job_id: String) = Action.async { implicit request =>

    val session_id = Session.requestSessionID(request) // Grab the Session ID

    (userManager ? GetUserActor(session_id)).mapTo[ActorRef].flatMap { userActor =>
      (userActor ? GetJob(job_id)).mapTo[Option[UserJob]].flatMap {

        case None => Future.successful(NotFound)

        case Some(job) =>

          // Switch on Job state to decide what to show
          job.getState match {

            // User has requested a job that is currently prepared
            case Prepared =>
              Logger.info("Prepared job requested")

              (userActor ? GetJobParams(job.job_id)).mapTo[Map[String, String]].map { res =>

                val toolframe = job.toolname match {
                  case "alnviz" => views.html.alnviz.form(Alnviz.inputForm.bind(res))
                  case "tcoffee" => views.html.tcoffee.form(Tcoffee.inputForm.bind(res))
                  case "hmmer3" => views.html.hmmer3.form(Hmmer3.inputForm.bind(res))
                  case "psiblast" => views.html.psiblast.form(Psiblast.inputForm.bind(res))
                  case "reformat" => views.html.reformat.form(Reformat.inputForm.bind(res))
                }

                Ok(views.html.general.submit(job.tool.toolname, toolframe, Some(job_id))).withSession {
                  Session.closeSessionRequest(request, session_id) // Send Session Cookie
                }
              }
          }
      }
    }

}
*/









/*
  /**
    * Asks the userActor to clear a job from the view
    *
    * @param job_id job_id of the job
    * @return
    */
  def clearJob(job_id: String) = Action.async { implicit request =>
    Logger.info("clear")

    val session_id = Session.requestSessionID(request)
    (userManager ? GetUserActor(session_id)).mapTo[ActorRef].map { userActor =>

      userActor ! ClearJob(job_id)
      Ok(Json.obj("job_id" -> job_id))
    }
  } */

/*
  }

  /**
    * Searches for a matching job_id
    *
    * @param jobIDLookup prefix of a job_id
    * @return
    */
  def findJobID(jobIDLookup: String) = Action.async { implicit request =>
    Future.successful {
      Ok(views.html.general.search(jobDB.suggestJobID(12345L, jobIDLookup)))
    }
  }

  /**
    * Returns a list of jobs a user owns
 *
    * @param user_id user_id of the user
    * @return
    */
  def showJobs(user_id : Long) = Action.async { implicit reqest =>
    Future.successful {
      Ok(views.html.general.search(jobDB.getJobsForUser(user_id)))
    }
  }


  /**
    * Adds a job_id to the current list of jobs
 *
    * @param job_id job_id of the job
    * @return
    */
  def addJobID(job_id: String) = Action.async { implicit request =>
    val session_id = Session.requestSessionID(request) // Grab the Session ID
    Logger.info("Adding Job for " + session_id + ", job_id to add: " + job_id)
    Future.successful {
      val jobSeq = jobDB.suggestJobID(12345L, job_id)
      jobSeq.headOption match {
        // Found jobs, list them now
        case Some(dbJob) =>
          (userManager ? GetUserActor(session_id)).mapTo[ActorRef].map { userActor =>
            userActor ! LoadJob(dbJob.job_id)
          }
          // This sends a NoContent header to the user, informing them that the request was ok
          // but that there is nothing to do on their side as we want to send this over websockets
          NoContent

        // no such job_id, send to NotFound (This is needed in case someone follows the route to try out stuff)
        case None =>
          NotFound
      }
    }
  }
}
*/
