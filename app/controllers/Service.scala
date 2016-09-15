package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager._
import akka.actor.ActorRef
import akka.util.Timeout
import models.Constants
import models.database.{Job, JobState}
import models.tel.TEL
import modules.tools.ToolMatcher
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection
import better.files._
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Success

/**
  *
  *
  * Created by lukas on 2/27/16.
  */
@Singleton
class Service @Inject() (webJarAssets     : WebJarAssets,
                     val messagesApi      : MessagesApi,
@NamedCache("userCache") implicit val userCache        : CacheApi,
                     val reactiveMongoApi : ReactiveMongoApi,
                     val tel              : TEL,
                     val toolMatcher      : ToolMatcher,
    @Named("jobManager") jobManager       : ActorRef)

                 extends Controller with I18nSupport
                                    with Constants
                                    with ReactiveMongoComponents
                                    with UserSessions {

  implicit val timeout = Timeout(1.seconds)

  def static(static: String) = Action { implicit request =>

    static match {

      case "sitemap" =>
        Ok(views.html.general.sitemap())

      // Frontend tools
      case "reformat" =>
        Ok(views.html.tools.forms.reformat(webJarAssets, "Utils"))

      case "patSearch" =>
        Ok(views.html.tools.forms.patSearch())
      case "extractIDs" =>
        Ok(views.html.tools.forms.extractIDs())
      case "alnvizfrontend" =>
        Ok(views.html.tools.forms.alnvizfrontend())

      case _ =>

        Ok(views.html.errors.pagenotfound()) //Bug: Mithril only accepts 200 to re-route

    }
  }


  // TODO  Handle Acknowledgement
  /**
    * User asks to delete the Job with the provided job_id
    *
    * @param mainID
    * @return
    */
  def delJob(mainID: String) = Action.async { implicit request =>
    getUser.map { user =>
      // TODO We go over the Websocket for this now, we may keep this for testing until production?
      jobManager ! DeleteJob(user.userID, BSONObjectID.parse(mainID).getOrElse(BSONObjectID.generate()))
      Ok.withSession(sessionCookie(request, user.sessionID.get))
    }
  }

  /**
    * Add a job to the view
    * @param mainIDString
    * @return
    */
  def addJob(mainIDString : String) = Action.async { implicit request =>
    getUser.map { user =>
      BSONObjectID.parse(mainIDString) match {
        case Success(mainID) =>
          jobManager ! AddJob(user.userID, mainID)
          Ok.withSession(sessionCookie(request, user.sessionID.get))
        case _ =>
          NotFound
      }
    }
  }

  /**
    * Return the job info page
    * @param mainIDString
    * @return
    */
  def showJobInfo(mainIDString: String) = Action.async { implicit request =>
    // Retrieve the user from the cache or the DB
    getUser.flatMap { user =>
      // Check if the ID is plausible (Right Format can be parsed into a BSON Object ID)
      BSONObjectID.parse(mainIDString) match {
        case Success(mainID) =>
          val futureJob = jobCollection.flatMap(_.find(BSONDocument(Job.IDDB -> mainID)).one[Job])
          futureJob.flatMap {
            case Some(job) =>
              // Return the Right Page for each Status
              job.status match {
                // Tell the user that the job is running at the moment
                case JobState.Running =>
                  Future.successful(Ok(views.html.jobs.running(job.jobID)))

                // Show the input form if the Job is Prepared or Submitted
                case JobState.Prepared | JobState.Submitted =>
                  val resultFiles = s"$jobPath$SEPARATOR${job.mainID.stringify}${SEPARATOR}params".toFile.list.map { f =>
                    f.name -> f.contentAsString
                  }.toMap
                  val toolFrame = toolMatcher.resultPreparedMatcher(job.tool, resultFiles)
                  Future.successful {
                    Ok(views.html.general.submit(tel, job.tool, toolFrame, Some(job)))
                      .withSession(sessionCookie(request, user.sessionID.get))
                  }

                // Since the job is Done show the Result pages
                case JobState.Done =>
                  val toolFrame = toolMatcher.resultDoneMatcher(job)
                  Future.successful(Ok(toolFrame)
                    .withSession(sessionCookie(request, user.sessionID.get)))

                // There was an error, show the user the Error page
                case JobState.Error =>
                  Future.successful(Ok(views.html.jobs.error(job.jobID))
                    .withSession(sessionCookie(request, user.sessionID.get)))

                // Illegal state
                case _ =>
                  println("Illegal Job State: " + job.status.toString)
                  Future.successful(NotFound)
              }
            case None =>
              Future.successful(NotFound)
          }
        case _ =>
          Future.successful(NotFound)
      }
    }
  }
}