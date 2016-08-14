package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager._
import akka.actor.ActorRef
import akka.util.Timeout
import models.database.{Job, JobState, User}
import models.sessions.Session
import akka.pattern.ask
import models.tools.{Alnviz, Hmmer3, Psiblast, Tcoffee}

import scala.concurrent.duration._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
  *
  *
  * Created by lukas on 2/27/16.
  */
@Singleton
class Service @Inject() (webJarAssets: WebJarAssets,
                         val messagesApi: MessagesApi,
                         @Named("jobManager") jobManager : ActorRef) extends Controller with I18nSupport {

  implicit val timeout = Timeout(1.seconds)


  def static(static : String)  = Action { implicit request =>

    static match {

      case "sitemap" =>
        Ok(views.html.general.sitemap()).withSession {
          Session.closeSessionRequest(request, Session.requestSessionID)
        }
      // Frontend tools
      case "reformat" =>

        val sessionID = Session.requestSessionID
        val user_o : Option[User] = Session.getUser

        Ok(views.html.tools.forms.reformat(webJarAssets,"Utils", user_o)).withSession {
          Session.closeSessionRequest(request, sessionID)
        }
        
      case "seq2gi" =>
        Ok(views.html.tools.forms.seq2gi()).withSession {
          Session.closeSessionRequest(request, Session.requestSessionID)
        }

      case _ =>

        Ok(views.html.errors.pagenotfound()) //Bug: Mithril only accepts 200 to re-route

    }
  }



  // TODO  Handle Acknowledgement
  /**
    * User asks to delete the Job with the provided job_id
    *
    * @param jobID
    * @return
    */
  def delJob(jobID: String) = Action { implicit request =>

    jobManager ! Delete(Session.requestSessionID, jobID)
    Ok
  }

  /*
  val addChild = Action.async(parse.json) { implicit request =>

    Logger.info("Add child Job received")

    val sessionID = Session.requestSessionID // Grab the Session ID

    (userManager ? GetUserActor(sessionID)).mapTo[ActorRef].map { userActor =>

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

  /**
    * Returns JobState and toolname for a given jobID
    *
    * @param jobID
    * @return
    */
  def jobInfo(jobID: String) = Action.async { implicit request =>

    val sessionID = Session.requestSessionID // Grab the Session ID

    (jobManager ? JobInfo(sessionID, jobID)).flatMap {

      case JobIDUnknown => Future.successful(NotFound)
      case PermissionDenied => Future.successful(NotFound)

      case job : Job =>

        // Decide what to show depending on the JobState
      job.status match {

        // User has requested a job whose state is Running
        case JobState.Running => Future.successful(Ok(views.html.job.running(jobID)).withSession {
          Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
        })

        case JobState.Prepared =>

          (jobManager ? Read(sessionID, jobID)).mapTo[Map[String, String]].map { res =>

            val toolframe = job.tool match {
              case "alnviz" => views.html.tools.forms.alnviz(Alnviz.inputForm.bind(res))
              case "tcoffee" => views.html.tools.forms.tcoffee(Tcoffee.inputForm.bind(res))
              case "hmmer3" => views.html.tools.forms.hmmer3(Hmmer3.inputForm.bind(res))
              case "psiblast" => views.html.tools.forms.psiblast(Psiblast.inputForm.bind(res))
            }

            Ok(views.html.general.submit(job.tool, toolframe, Some(jobID))).withSession {
              Session.closeSessionRequest(request, sessionID) // Send Session Cookie
            }
          }

        // User requested job whose execution is done
        case JobState.Done =>

          val toolframe = job.tool match {

            //  The tool anlviz just returns the BioJS MSA Viewer page
            case "alnviz" =>
              val vis = Map("BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/result"))
              views.html.job.result(vis, jobID, job.tool)


            // For T-Coffee, we provide a simple alignment visualiation and the BioJS View
            case "tcoffee" =>

              val vis = Map(
                "Simple" -> views.html.visualization.alignment.simple(s"/files/${job.mainID.stringify}/sequences.clustalw_aln"),
                "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/sequences.clustalw_aln"))

              views.html.job.result(vis, jobID, job.tool)


            case "reformatb" =>

              val vis = Map(
                "Simple" -> views.html.visualization.alignment.simple(s"/files/${job.mainID.stringify}/sequences.clustalw_aln"),
                "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/sequences.clustalw_aln"))

              views.html.job.result(vis, jobID, job.tool)

            case "psiblast" =>

              val vis = Map(
                "Results" -> views.html.visualization.alignment.blastvis(s"/files/${job.mainID.stringify}/out.psiblastp"),
                "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/sequences.clustalw_aln"),
                "Evalue" -> views.html.visualization.alignment.evalues(s"/files/${job.mainID.stringify}/evalues.dat"))

              views.html.job.result(vis, jobID, job.tool)

            // Hmmer just provides a simple file viewer.
            case "hmmer3" => views.html.visualization.general.fileview(
              Array(s"/files/${job.mainID.stringify}/domtbl",
                    s"/files/${job.mainID.stringify}/outfile",
                    s"/files/${job.mainID.stringify}/outfile_multi_sto",
                    s"/files/${job.mainID.stringify}/tbl"))
          }
          Future.successful(Ok(toolframe).withSession {
            Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
          })

        case JobState.Error =>

          Future.successful(Ok(views.html.job.error(jobID)).withSession {
            Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
          })
      }
    }
  }
}


  /*
  def getJob(job_id: String) = Action.async { implicit request =>

    val sessionID = Session.requestSessionID // Grab the Session ID

    (userManager ? GetUserActor(sessionID)).mapTo[ActorRef].flatMap { userActor =>
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
                  Session.closeSessionRequest(request, sessionID) // Send Session Cookie
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

    val sessionID = Session.requestSessionID
    (userManager ? GetUserActor(sessionID)).mapTo[ActorRef].map { userActor =>

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
    val sessionID = Session.requestSessionID // Grab the Session ID
    Logger.info("Adding Job for " + sessionID + ", job_id to add: " + job_id)
    Future.successful {
      val jobSeq = jobDB.suggestJobID(12345L, job_id)
      jobSeq.headOption match {
        // Found jobs, list them now
        case Some(dbJob) =>
          (userManager ? GetUserActor(sessionID)).mapTo[ActorRef].map { userActor =>
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
