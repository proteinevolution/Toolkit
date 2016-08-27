package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager.Prepare
import akka.actor.ActorRef
import akka.stream.Materializer
import akka.util.Timeout
import models.database.JobState.Done
import models.database.{Job, JobHash, Session}
import models.search.JobDAO
import models.tools._
import modules.tools.FNV
import play.Logger
import play.api.cache._
import play.api.libs.json._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONInteger, BSONObjectID, BSONDocument}

import scala.concurrent.duration._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global
import better.files._

import scala.util.{Failure, Success}


@Singleton
class Tool @Inject()(val messagesApi      : MessagesApi,
@NamedCache("userCache") userCache        : CacheApi,
                     val reactiveMongoApi : ReactiveMongoApi,
            implicit val mat              : Materializer,
                     val jobDao : JobDAO,
    @Named("jobManager") jobManager       : ActorRef) extends Controller with I18nSupport with UserSessions {

  implicit val timeout = Timeout(5.seconds)
  def userCollection = reactiveMongoApi.database.map(_.collection("jobs").as[BSONCollection](FailoverStrategy()))
  def hashCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobhashes"))
  def jobCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs"))



  def submit(toolname: String, start : Boolean, jobID : Option[String]) = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>

    // Fetch the job ID from the submission, might be the empty string
    //val jobID = request.body.asFormUrlEncoded.get("jobid").head --- There won't be a job ID in the request

    // TODO replace with reflection to avoid the need to mention each tool explicitly here
    val form = toolname match {
      case "alnviz" => Some(Alnviz.inputForm)
      case "tcoffee" => Some(Tcoffee.inputForm)
      case "hmmer3" => Some(Hmmer3.inputForm)
      case "hhpred" => Some(HHpred.inputForm)
      case "hhblits" => Some(HHblits.inputForm)
      case "psiblast" => Some(Psiblast.inputForm)
      case "mafft" => Some(Mafft.inputForm)
      case "reformatb" => Some(Reformatb.inputForm) // cluster version of reformat
      case "clans" => Some(Clans.inputForm)
      case _ => None
    }

    if (form.isEmpty)
      NotFound

    else {
      val boundForm = form.get.bindFromRequest // <- params


      lazy val DB = boundForm.data.getOrElse("standarddb","").toFile  // get hold of the database in use
      lazy val jobByteArray = boundForm.data.toString().getBytes // convert params to hashable byte array
      lazy val inputHash = FNV.hash64(jobByteArray).toString()


      lazy val dbName = {
        boundForm.data.get("standarddb") match {
          case None => Some("none")
          case _ => Some(DB.name)
        }
      }

      lazy val dbMtime = {
        boundForm.data.get("standarddb") match {
          case None => Some("1970-01-01T00:00:00Z")
          case _ => Some("2016-08-09T12:46:51Z")
        }
      }


      lazy val hashQuery = jobDao.matchHash(inputHash, dbName, dbMtime)

      hashQuery.onComplete({
        case Success(s) =>
          println("success: " + s)
          println("hits: " + s.totalHits)

          if(s.totalHits >= 1) {

            for(x <- s.getHits.getHits) {

              println(x.getId)


              jobCollection.flatMap(_.find(BSONDocument(Job.IDDB -> BSONObjectID(x.getId))).one[Job]).foreach {



                case Some(oldJob) =>
                  if (oldJob.status != Done) {
                    println("job with same signature found but job failed, should submit the job again")
                    jobCollection.flatMap(_.remove(BSONDocument(Job.IDDB -> BSONObjectID(x.getId)))) // we should delete failed jobs only here because keeping them is normally useful for debuggin and statistics
                  }

                  println("job found: " + oldJob.tool)


                case None => println("Error: job in index but not in database")

              }
            }

          }

         // else if (s.totalHits > 1) { println("too many jobs with same signature") } TODO we should take care that the exact same job exists only once in the database


          else { println("no hits found, job should be processed") }


        case Failure(exception) =>
          println("An error has occured: " + exception)

      })



      boundForm.fold(
        formWithErrors => {

          BadRequest("There was an error with the Form")
        },



        _ => jobManager ! Prepare(user, jobID, toolname, boundForm.data, start = start)
      )
      Ok.withSession(sessionCookie(request, user.sessionID.get))

    }
  }
  }
}


