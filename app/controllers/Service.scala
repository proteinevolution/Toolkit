package controllers

import java.io.{FileInputStream, ObjectInputStream}
import javax.inject.{Inject, Singleton}

import akka.util.Timeout
import models.tools.ToolModel
import models.{Constants, Values}
import models.database._
import modules.tel.TEL
import play.api.Logger
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import better.files._
import models.database.JobState
import models.tools.ToolModel._
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
                               @NamedCache("toolitemCache") val toolitemCache   : CacheApi,
                               @NamedCache("jobitem") jobitemCache              : CacheApi,
                               val reactiveMongoApi                             : ReactiveMongoApi,
                               val tel                                          : TEL,
                               val values                                       : Values)
                               extends Controller with I18nSupport
                                                  with Constants
                                                  with ReactiveMongoComponents
                                                  with UserSessions
                                                  with CommonModule {

  implicit val timeout = Timeout(1.seconds)

  def static(static: String) = Action { implicit request =>

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


  // TODO Add validation
  implicit val toolitemWrites: Writes[Toolitem] = (
    (JsPath \ "toolname").write[String] and
      (JsPath \ "toolnameLong").write[String] and
      (JsPath \ "toolnameAbbrev").write[String] and
      (JsPath \ "category").write[String] and
      (JsPath \ "optional").write[String] and
      (JsPath \ "params").write[Seq[(String, Seq[(String, Seq[(String, String)])])]]
    ) (unlift(Toolitem.unapply))



  implicit val jobitemWrites: Writes[Jobitem] = (
      (JsPath \ "mainID").write[String] and
      (JsPath \ "newMainID").write[String] and
      (JsPath \ "jobID").write[String] and
      (JsPath \ "state").write[JobState] and
      (JsPath \ "ownerName").write[String] and
      (JsPath \ "createdOn").write[String] and
      (JsPath \ "toolitem").write[Toolitem] and
      (JsPath \ "views").write[Seq[(String, Html)]] and
      (JsPath \ "paramValues").write[Map[String, String]](play.api.libs.json.Writes.mapWrites[String])
    ) (unlift(Jobitem.unapply))


  def getTool(toolname: String) = Action {

    val toolitemJson = Json.toJson(toolitemCache.getOrElse(toolname) {
      val x = ToolModel.toolMap(toolname).toolitem(values) // Reset toolitem in cache
      toolitemCache.set(toolname, x)
      x
    })
    Ok(toolitemJson)
  }



  def getJob(jobID: String) = Action.async { implicit request =>


    Logger.info("getJobReached")

    selectJob(jobID).flatMap {

          case Some(job) =>
            Logger.info("Requested job has been found in MongoDB, the jobState is " + job.status)

            val toolModel = ToolModel.toolMap(job.tool)

            val toolitem = toolitemCache.getOrElse(job.tool) {
              val x = toolModel.toolitem(values) // Reset toolitem in cache
              toolitemCache.set(job.tool, x)
              x
            }
            val ownerName =
              if (job.isPrivate) {
                findUser(BSONDocument(User.IDDB -> job.ownerID.get)).map{
                  case Some(owner) =>
                    owner.userData match {
                      case Some(ownerData) => // Owner is registered
                        s"Owner: ${ownerData.nameLogin}"
                      case None => // Owner is not registered
                        "Owner: Guest"
                    }
                  case None => // User does no longer exist in the Database.
                    "Deleted User"
                }
              } else {
                Future.successful("Public Job")
              }
            // The jobState decides which views will be appended to the job

            val jobViews: Seq[(String, Html)] = job.status match {

              case Done =>
                toolModel.results.map { resultName =>
                  resultName -> views.html.jobs.resultpanel(resultName, job.jobID, job.tool, jobPath)
                }

              // All other views are currently computed on Clientside
              case _ => Seq.empty
            }

            // Read parameters from serialized file
            val ois = new ObjectInputStream(new FileInputStream((jobPath/jobID/"sparam").pathAsString))
            val paramValues = ois.readObject().asInstanceOf[Map[String, String]]
            ois.close()

            ownerName.map{ ownerN =>
              Ok(Json.toJson(
                Jobitem(job.mainID.stringify,
                        BSONObjectID.generate().stringify, // Used for resubmitting
                        job.jobID,
                        job.status,
                        ownerN,
                        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").print(job.dateCreated.get),
                        toolitem,
                        jobViews,
                        paramValues)))
            }

          case _ =>
            Logger.info("Job could not be found")
            Future.successful(NotFound)
        }
  }
}