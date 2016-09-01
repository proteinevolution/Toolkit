package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager.Prepare
import akka.actor.ActorRef
import akka.stream.Materializer
import akka.util.Timeout
import models.database.JobState.{Running, Done}
import models.database.Job
import models.search.JobDAO
import models.tools.ToolModel

import modules.tools.{ToolMatcher, FNV}
import play.api.cache._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONObjectID, BSONDocument}

import scala.concurrent.duration._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global
import better.files._

import scala.util.{Failure, Success}

object Tool {

  lazy val tools:Seq[ToolModel] = ToolModel.values // this list is completely dynamic and depends only on the case objects in the tool model. frontend tools are excluded at the moment.

}


@Singleton
final class Tool @Inject()(val messagesApi      : MessagesApi,
                           @NamedCache("userCache") userCache : CacheApi,
                           val reactiveMongoApi : ReactiveMongoApi,
                           implicit val mat     : Materializer,
                           val jobDao           : JobDAO,
                           val toolMatcher      : ToolMatcher,
                           @Named("jobManager") jobManager : ActorRef) extends Controller with I18nSupport with UserSessions with Common {

  implicit val timeout = Timeout(5.seconds)


  def submit(toolname: String, start : Boolean, jobID : Option[String]) = Action.async { implicit request =>


    getUser(request, userCollection, userCache).map { user =>


      // Fetch the job ID from the submission, might be the empty string
      //val jobID = request.body.asFormUrlEncoded.get("jobid").head --- There won't be a job ID in the request


      val form = toolMatcher.formMatcher(toolname)

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
                    if (oldJob.status != Done && oldJob.status != Running) {
                      println("job with same signature found but job failed, should submit the job again")
                      jobCollection.flatMap(_.remove(BSONDocument(Job.IDDB -> BSONObjectID(x.getId)))) // we should delete failed jobs only here because keeping them is normally useful for debuggin and statistics
                    }
                    else {
                      println("job found: " + oldJob.tool)

                      // TODO redirect here to the first found job or better: trigger a popup


                    }

                  case None => println("[WARNING]: job in index but not in database")

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


