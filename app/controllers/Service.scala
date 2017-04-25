package controllers

import java.io.{FileInputStream, ObjectInputStream}
import javax.inject.{Inject, Singleton}

import akka.util.Timeout
import models.Constants
import models.database.jobs.{Done, JobState, Jobitem}
import models.database.users.User
import play.api.Logger
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller, Request}
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import better.files._
import models.tools.{Param, ToolFactory, Toolitem}
import modules.{CommonModule, LocationProvider}
import org.joda.time.format.DateTimeFormat
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.twirl.api.Html
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  *
  *
  * Created by lukas on 2/27/16.
  */
@Singleton
final class Service @Inject() (webJarAssets                                     : WebJarAssets,
                               val messagesApi                                  : MessagesApi,
                               @NamedCache("userCache") implicit val userCache  : CacheApi,
                               implicit val locationProvider                    : LocationProvider,
                               toolFactory                                      : ToolFactory,
                               val reactiveMongoApi                             : ReactiveMongoApi)
                               extends Controller with I18nSupport
                                                  with Constants
                                                  with ReactiveMongoComponents
                                                  with UserSessions
                                                  with CommonModule {

  implicit val timeout = Timeout(1.seconds)

  def static(static: String) : Action[AnyContent] = Action { implicit request =>

    static match {

      case "sitemap" =>
        Ok(views.html.general.sitemap())

      // Frontend tools
      case "reformat" =>
        Ok(views.html.tools.forms.reformat(webJarAssets, "Utils"))

      case _ =>

        Ok(views.html.errors.pagenotfound()) //Bug: Mithril only accepts 200 to re-route

    }
  }
  // Allows serialization of tuples
  implicit def tuple2Reads[A, B](implicit aReads: Reads[A], bReads: Reads[B]): Reads[(A, B)] = Reads[(A, B)] {
    case JsArray(arr) if arr.size == 2 => for {
      a <- aReads.reads(arr.head)
      b <- bReads.reads(arr(1))
    } yield (a, b)
    case _ => JsError(Seq(JsPath() -> Seq(ValidationError("Expected array of three elements"))))
  }

  implicit def tuple2Writes[A, B](implicit a: Writes[A], b: Writes[B]): Writes[(A, B)] = new Writes[(A, B)] {
    def writes(tuple: (A, B)) = JsArray(Seq(a.writes(tuple._1), b.writes(tuple._2)))
  }

  implicit def htmlWrites : Writes[Html] = new Writes[Html] {

    def writes(html: Html) = JsString(html.body)
  }

  implicit val toolitemWrites: Writes[Toolitem] = (
    (JsPath \ "toolname").write[String] and
      (JsPath \ "toolnameLong").write[String] and
      (JsPath \ "toolnameAbbrev").write[String] and
      (JsPath \ "category").write[String] and
      (JsPath \ "optional").write[String] and
      (JsPath \ "params").write[Seq[(String, Seq[Param])]]
    ) (unlift(Toolitem.unapply))

  implicit val jobitemWrites: Writes[Jobitem] = (
      (JsPath \ "mainID").write[String] and
      (JsPath \ "newMainID").write[String] and
      (JsPath \ "jobID").write[String] and
      (JsPath \ "project").write[String] and
      (JsPath \ "state").write[JobState] and
      (JsPath \ "ownerName").write[String] and
      (JsPath \ "createdOn").write[String] and
      (JsPath \ "toolitem").write[Toolitem] and
      (JsPath \ "views").write[Seq[(String, Html)]] and
      (JsPath \ "paramValues").write[Map[String, String]](play.api.libs.json.Writes.mapWrites[String])
    ) (unlift(Jobitem.unapply))


  def getTool(toolname: String) = Action {
    toolFactory.values.get(toolname) match {
      case Some(tool) => Ok(Json.toJson(tool.toolitem))
      case None => NotFound
    }
  }
  def getJob(jobID: String) : Action[AnyContent] = Action.async { implicit request =>
    selectJob(jobID).flatMap {
      case Some(job) =>
        job.deletion match {
          case None =>
            Logger.info("Requested job has been found in MongoDB, the jobState is " + job.status)
            val toolitem = toolFactory.values(job.tool).toolitem
            val ownerName =
              if (job.isPrivate) {
                findUser(BSONDocument(User.IDDB -> job.ownerID.get)).map{
                  case Some(owner) =>
                    owner.userData match {
                      case Some(ownerData) => // Owner is registered
                        ownerData.nameLogin
                      case None => // Owner is not registered
                        "Guest"
                    }
                  case None => // User does no longer exist in the Database.
                    "Unknown User"
                }
              } else {
                Future.successful("Public Job")
              }
            // The jobState decides which views will be appended to the job

            val jobViews: Future[Seq[(String, Html)]] = job.status match {

              case Done => toolFactory.getResults(job.jobID, job.tool, jobPath)

              // All other views are currently computed on Clientside
              case _ => Future.successful(Nil)
            }
            // Read parameters from serialized file
            val paramValues: Map[String, String] = {
              if((jobPath/jobID/"sparam").exists) {
                val ois = new ObjectInputStream(new FileInputStream((jobPath/jobID/"sparam").pathAsString))
                val x = ois.readObject().asInstanceOf[Map[String, String]]
                ois.close()
                x
              } else {
                Map.empty[String, String]
              }
            }
            ownerName.flatMap { ownerN =>
              jobViews.map { jobViewsN =>
                Ok(Json.toJson(
                  Jobitem(job.mainID.stringify,
                    BSONObjectID.generate().stringify, // Used for resubmitting
                    job.jobID,
                    BSONObjectID.generate().stringify,
                    job.status,
                    ownerN,
                    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").print(job.dateCreated.get),
                    toolitem,
                    jobViewsN,
                    paramValues)))
              }
            }

          case Some(deletionReason) =>
            // The job was deleted, do not show it to the user.
            Logger.info("Job was found but deleted.")
            Future.successful(NotFound)
        }
    case _ =>
      Logger.info("Job could not be found")
      Future.successful(NotFound)
    }
  }
}