package controllers

import javax.inject.{Inject, Singleton}

import models.{Constants, Values}
import models.database.{Job, JobState}
import models.database.JobState.JobState
import models.tools.ToolModel2
import models.tools.ToolModel2.Toolitem
import org.joda.time.format.DateTimeFormat
import play.api.cache.{CacheApi, _}
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import play.api.libs.functional.syntax._
import play.modules.reactivemongo.ReactiveMongoApi
import play.twirl.api.Html
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import better.files._

import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global



/**
  *  Just for some Testing purposes
  *
  * Created by lzimmermann on 10/14/16.
  */
@Singleton
class TestController @Inject() (val values: Values,
                                val reactiveMongoApi : ReactiveMongoApi,
                                @NamedCache("userCache") implicit val userCache : CacheApi,
                                @NamedCache("toolitemCache") val toolitemCache: CacheApi)
  extends Controller with UserSessions with Constants {

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



/*
    @if(jobOption.get.status == JobState.Done) {
        @for((key, value) <- resultsections.get) {

             <div class="tabs-panel" id="tabpanel-@key">
                   @jobs.resultpanel(key, value, jobOption.get.mainID.stringify)
                </div>
                views.html.jobs.parampanel

*/


// Retrieve the user from the cache or the DB
/*
    getUser.flatMap { user =>
      // Check if the ID is plausible (Right Format can be parsed into a BSON Object ID)
      BSONObjectID.parse(mainIDString) match {
        case Success(mainID) =>
          val futureJob = jobCollection.flatMap(_.find(BSONDocument(Job.IDDB -> mainID)).one[Job])
          futureJob.flatMap {


            case Some(job) => Future.successful(NotFound)



            case None =>
              Future.successful(NotFound)
          }
        case _ =>
          Future.successful(NotFound)
      }
    } */



/*
implicit val jobitemReads: Reads[Jobitem] = (
  (JsPath \ "jobID").read[String] and
    (JsPath \ "state").read[JobState] and
    (JsPath \ "createdOn").read[String] and
    (JsPath \ "toolitem").read[Toolitem] and
    (JsPath \ "views").read[Seq[(String, Html)]]
  )(Jobitem.apply _)

implicit val toolitemReads: Reads[Toolitem] = (
(JsPath \ "toolname").read[String] and
(JsPath \ "toolnameLong").read[String] and
(JsPath \ "toolnameAbbrev").read[String] and
(JsPath \ "category").read[String] and
(JsPath \ "params").read[Seq[(String, Seq[(String, Seq[(String, String)])])]]
)(Toolitem.apply _)
*/