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
import better.files._
import models.database.JobState.JobState
import models.tools.ToolModel2
import models.tools.ToolModel2.Toolitem
import modules.Common
import org.joda.time.format.DateTimeFormat
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.twirl.api.Html
import reactivemongo.bson.{BSONDocument, BSONInteger, BSONObjectID, BSONValue}
import models.database.JobState._
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
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
                         @NamedCache("toolitemCache") val toolitemCache: CacheApi,
                     val reactiveMongoApi : ReactiveMongoApi,
                     val tel              : TEL,
                         final val values : Values,
                     val toolMatcher      : ToolMatcher,

    @Named("jobManager") jobManager       : ActorRef)

                 extends Controller with I18nSupport
                                    with Constants
                                    with ReactiveMongoComponents
                                    with UserSessions with Common {

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


  def listJobs = Action.async { implicit request =>

    getUser.flatMap { user =>

      findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> user.jobs))).map { jobs =>
        Ok(Json.toJson( jobs.map(_.cleaned)))
      }
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


  implicit def htmlWrites = new Writes[Html] {

    def writes(html: Html) = JsString(html.body)
  }


  // TODO Add validation
  implicit val toolitemWrites: Writes[Toolitem] = (
    (JsPath \ "toolname").write[String] and
      (JsPath \ "toolnameLong").write[String] and
      (JsPath \ "toolnameAbbrev").write[String] and
      (JsPath \ "category").write[String] and
      (JsPath \ "params").write[Seq[(String, Seq[(String, Seq[(String, String)])])]]
    ) (unlift(Toolitem.unapply))


  // Server returns such an object when asked for a job
  case class Jobitem(jobID: String,
                     state: JobState,
                     createdOn: String,
                     toolitem: Toolitem,
                     views: Seq[(String, Html)],
                     paramValues: Map[String, String])

  implicit val jobitemWrites: Writes[Jobitem] = (
    (JsPath \ "jobID").write[String] and
      (JsPath \ "state").write[JobState] and
      (JsPath \ "createdOn").write[String] and
      (JsPath \ "toolitem").write[Toolitem] and
      (JsPath \ "views").write[Seq[(String, Html)]] and
      (JsPath \ "paramValues").write[Map[String, String]](play.api.libs.json.Writes.mapWrites[String])
    ) (unlift(Jobitem.unapply))


  def getTool(toolname: String) = Action {

    Ok(Json.toJson(toolitemCache.getOrElse(toolname) {
      val x = ToolModel2.toolMap(toolname).toolitem(values) // Reset toolitem in cache
      toolitemCache.set(toolname, x)
      x
    }))
  }



  def getJob(mainIDString: String) = Action.async { implicit request =>

    BSONObjectID.parse(mainIDString) match {

      case Success(mainID) =>

        jobCollection.flatMap(_.find(BSONDocument(Job.IDDB -> mainID)).one[Job]).map {

          case Some(job) =>
            val toolModel = ToolModel2.toolMap(job.tool)

            val toolitem = toolitemCache.getOrElse(job.tool) {
              val x = toolModel.toolitem(values) // Reset toolitem in cache
              toolitemCache.set(job.tool, x)
              x
            }
            // The jobState decides which views will be appended to the job
            val jobViews: Seq[(String, Html)] = job.status match {

              case JobState.Running => Seq(
                "Running" -> Html("Job is currently being executed"))

              case JobState.Done => toolModel.results.map { kv =>

                kv._1 -> views.html.jobs.resultpanel(kv._1, kv._2, job.mainID.stringify)
              }.toSeq

              case JobState.Prepared => Seq.empty
            }
            val paramValues = s"$jobPath$SEPARATOR${job.mainID.stringify}${SEPARATOR}params".toFile.list.map{ file =>

              file.name -> file.contentAsString
            }.toMap


            Ok(Json.toJson(Jobitem(job.jobID,
              job.status, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").print(job.dateCreated.get) ,toolitem,  jobViews, paramValues)))
        }
    }
  }



}