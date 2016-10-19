package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager._
import akka.actor.ActorRef
import akka.util.Timeout
import models.{Constants, Values}
import models.database.{Job, JobState}
import models.tel.TEL
import modules.tools.ToolMatcher
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection
import better.files._
import models.tools.ToolModel2
import play.api.Logger
import reactivemongo.bson.{BSONDocument, BSONObjectID}

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
                         final val values : Values,
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
      case "alnvizfrontend" =>
        Ok(views.html.tools.forms.alnvizfrontend(webJarAssets, "Alignment"))

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

}