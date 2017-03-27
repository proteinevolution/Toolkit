package controllers

import models.database.jobs.Job
import play.Logger
import models.Constants
import play.api.cache._
import play.api.libs.json.{JsNull, Json}
import javax.inject.{Inject, Singleton}

import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import models.search.JobDAO
import models.tools.ToolFactory
import modules.{CommonModule, LocationProvider}
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.Future
import scala.language.postfixOps

@Singleton
final class Search @Inject() (@NamedCache("userCache") implicit val userCache : CacheApi,
                              implicit val locationProvider: LocationProvider,
                              val reactiveMongoApi : ReactiveMongoApi,
                              toolFactory: ToolFactory,
                              val jobDao           : JobDAO)
                              extends Controller with Constants
                                                 with ReactiveMongoComponents
                                                 with UserSessions
                                                 with CommonModule {



  def ac(queryString : String) : Action[AnyContent] = Action.async{ implicit request =>
    jobDao.jobIDcompletionSuggester(queryString).map { richSearchResponse =>
      val jobIDEntries = richSearchResponse.suggestion("jobID")
      if (jobIDEntries.size > 0) {

        val resp = jobIDEntries.entry(queryString).optionsText.toList

        Ok(Json.toJson(resp))

      } else {

        NotFound

      }
    }
  }

  def autoComplete(queryString : String) : Action[AnyContent] = Action.async{ implicit request =>
    getUser.flatMap { user =>
      val mainIDStrings : Future[List[String]] =
        // Find out if the user looks for a certain tool or for a jobID

        if(toolFactory.values.get(queryString).isDefined) {
        //if (ToolModel.toolMap.get(queryString).isDefined) {
          // Find the Jobs with the matching tool
          Logger.info("user is looking for tool: " + queryString)
          jobDao.jobsWithTool(queryString, user.userID).map(_.getHits.hits().toList.map(_.id()))
        } else {
          // Grab Job ID auto completions
          val jobIDSuggestions = jobDao.jobIDcompletionSuggester(queryString).map(_.suggestion("jobID").entry(queryString).optionsText.toList)
          jobIDSuggestions.map(ids => Logger.info("Found Strings: " + ids.toString()))

          // Search for jobIDs in ES
          val searchHits = jobIDSuggestions.flatMap(jobIDSuggestions => jobDao.getJobIDs(jobIDSuggestions)).map(_.getHits)
          //searchHits.map(ids => Logger.info("Found Hits: " + ids.totalHits()))
          // Grab main IDs from the hits
          searchHits.map(_.hits().toList.map(_.id()))
        }

      // Convert to BSON mainIDs
      val futureMainIDs = mainIDStrings.map(_.map(mainIDString => BSONObjectID.parse(mainIDString).get))
      //futureMainIDs.map(ids => Logger.info("mainID: " + ids.toString()))

      // Grab Job Objects from the Database
      futureMainIDs.map(mainIDs => findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> mainIDs)))).flatMap{ jobs =>
        //jobs.map(joblist => Logger.info("Final Result: " + joblist.toString()))
        jobs.map(_.map(_.cleaned())).map(jobJs => Ok(Json.toJson(jobJs)))
      }
    }
  }


  def elasticSearch(userID : BSONObjectID, queryString : String) : Future[List[Job]] = {
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


  def getJob(queryString : String) : Action[AnyContent] = Action.async { implicit request =>
    // Retrieve the user from the cache or the DB
    getUser.flatMap { user =>
      elasticSearch(user.userID, queryString).map{ jobList =>
        Ok(Json.obj("jobs" -> jobList.map(_.cleaned())))
      }
    }
  }


  def get : Action[AnyContent] = Action.async { implicit request =>
    // Retrieve the jobs from the DB
    getUser.flatMap { user =>
      findJobs(BSONDocument(Job.OWNERID -> user.userID)).map{ jobs =>
        user.userData match {
          case Some(userData) =>
            val jobWithUserNames = jobs.map{ job =>
              job.withOwnerName(userData.nameLogin)
            }
            Ok(Json.toJson(jobWithUserNames))
          case None =>
            val jobWithUserNames = jobs.map{ job =>
              job.withOwnerName("-")
            }
            Ok(Json.toJson(jobWithUserNames))
        }
      }
    }
  }


  def getCleaned : Action[AnyContent] = Action.async { implicit request =>
    // Retrieve the jobs from the DB
    getUser.flatMap { user =>
      findJobs(BSONDocument(Job.OWNERID -> user.userID)).map{ jobs =>
        Ok(Json.toJson(jobs.map(_.cleaned())))
      }
    }
  }

  /**
    * Returns a json object containing both the last updated job and the most recent total number of jobs.
    * @return
    */
  def getIndexPageInfo : Action[AnyContent] = Action.async { implicit request=>
    getUser.flatMap { user =>
      findSortedJob(BSONDocument(Job.OWNERID -> user.userID), BSONDocument(Job.DATEUPDATED -> -1)).flatMap{ lastJob =>
        countJobs(BSONDocument(Job.OWNERID -> user.userID)).map { count =>
          Ok(Json.obj("lastJob" -> lastJob.map(_.cleaned()), "totalJobs" -> count))
        }
      }
    }
  }

  def checkJobID(jobID : String) : Action[AnyContent] = Action.async{
    jobDao.existsJobID(jobID).map{ richSearchResponse =>
      val jobIDExists : Boolean = richSearchResponse.getHits.getTotalHits > 0
      Logger.info("Looked for jobID: " + jobID + " Found: " + jobIDExists)
      Ok(Json.obj("exists" -> jobIDExists))
    }
  }


}
