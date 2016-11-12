package controllers


import akka.actor.ActorRef
import models.Constants
import models.database.Job
import play.api.cache._
import play.api.libs.json.Json
import javax.inject.{Named, Singleton, Inject}
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.ExecutionContext.Implicits.global
import models.search.JobDAO
import play.api.mvc.{Action, Controller}
import scala.concurrent.Future


@Singleton
final class Search @Inject() (
          @NamedCache("userCache") implicit val userCache        : CacheApi,
                               val reactiveMongoApi : ReactiveMongoApi,
                               val jobDao           : JobDAO)
                           extends Controller with Constants
                                              with ReactiveMongoComponents
                                              with UserSessions {

  def AutoComplete(userID : BSONObjectID, queryString : String) = {
    jobDao.findAutoCompleteJobID(queryString).map { richSearchResponse =>
      val jobIDEntries = richSearchResponse.suggestion("jobID")
      if (jobIDEntries.size > 0) {
        jobIDEntries.entry(queryString).optionsText.toList
      } else {
        List.empty[String]
      }
    }
  }

  def ElasticSearch(userID : BSONObjectID, queryString : String) = {
    jobDao.fuzzySearchJobID(queryString).flatMap { richSearchResponse =>
      if (richSearchResponse.totalHits > 0) {
        val jobIDEntries = richSearchResponse.getHits.getHits
        val mainIDs      = jobIDEntries.toList.map(hit => BSONObjectID.parse(hit.id).get)

        // Collect the list of jobs
        findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in"-> mainIDs)))
      } else {
        Future.successful(List.empty[Job])
      }
    }
  }

  def getJob(queryString : String) = Action.async { implicit request =>
    // Retrieve the user from the cache or the DB
    getUser.flatMap { user =>
      ElasticSearch(user.userID, queryString).map{ jobList =>
        Ok(Json.obj("jobs" -> jobList.map(_.cleaned())))
      }
    }
  }

  def checkJobID(jobID : String) = Action.async{
    jobDao.existsJobID(jobID).map{ richSearchResponse =>
      Ok(Json.obj("exists" -> {richSearchResponse.getHits.getTotalHits > 0}))
    }
  }
}
