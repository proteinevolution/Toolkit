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
      val toolOpt : Option[models.tools.Tool] = toolFactory.values.values.find(_.isToolName(queryString))
      Logger.info("user is looking for: " + queryString + " Found Tool: " + toolOpt)
      // Find out if the user looks for a certain tool or for a jobID
      toolOpt match {
        case Some(tool) => // Find the Jobs with the matching tool
          val mainIDStrings : Future[List[String]] =
            jobDao.jobsWithTool(tool.toolNameShort, user.userID).map(_.getHits.hits().toList.map(_.id()))
          // Convert to BSON mainIDs
          val futureMainIDs = mainIDStrings.map(_.map(mainIDString => BSONObjectID.parse(mainIDString).get))
          // Grab Job Objects from the Database
          futureMainIDs.map(mainIDs => findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> mainIDs)))).flatMap{ jobs =>
            //jobs.map(joblist => Logger.info("Final Result: " + joblist.toString()))
            jobs.map(_.map(_.cleaned())).map(jobJs => Ok(Json.toJson(jobJs)))
          }
        case None =>       // Grab Job ID auto completions
//            val jobIDSuggestions = jobDao.jobIDcompletionSuggester(queryString)
//              .map(_.suggestion("jobIDfield").entries.flatMap(_.options.map(_.text)).toList)
//            jobIDSuggestions.map(ids => Logger.info("Found Strings: " + ids.mkString(", ")))
//
//            // Search for jobIDs in ES
//            val searchHits = jobIDSuggestions.flatMap(jobIDSuggestions => jobDao.getJobIDs(jobIDSuggestions)).map(_.getHits)
//            //searchHits.map(ids => Logger.info("Found Hits: " + ids.totalHits()))
//            // Grab main IDs from the hits
//            searchHits.map(_.hits().toList.map(_.id()))
          //val jobIDP = s"($queryString.*)".r
          findJobs(BSONDocument(Job.JOBID -> BSONDocument("$regex" -> queryString))).map{ jobs =>
            val jobsFiltered = jobs.filter(job => job.ownerID.contains(user.userID) && job.deletion.isEmpty)
            Ok(Json.toJson(jobsFiltered.map(_.cleaned())))
          }
      }
    }
  }


  def existsTool(queryString : String) : Action[AnyContent] = Action.async { implicit request =>

    getUser.flatMap{ user =>
      val toolOpt : Option[models.tools.Tool] = toolFactory.values.values.find(_.isToolName(queryString))
      toolOpt match {
        case Some(tool) => Future.successful(Ok(Json.toJson(true)))
        case None => Future.successful(NotFound)
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
      findJobs(BSONDocument(Job.OWNERID -> user.userID, Job.DELETION -> BSONDocument("$exists" -> false))).map{ jobs =>
        Ok(Json.toJson(jobs.map(_.jobManagerJob())))
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
    *
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

  def checkJobID(jobID : String, resubmit : Boolean = false) : Action[AnyContent] = Action.async{
    val jobIDCompleteMessPattern = "(_+.*|.*_+)+".r // _ __ __x _x_ should be culled out from the start
    val jobIDNoVersionPattern = "([0-9a-zA-Z_]+)".r
    val jobVersionPattern = "(_(\\d{1,3}))?".r
    val jobIDPattern = (jobIDNoVersionPattern.regex + jobVersionPattern.regex).r
    val foundMainJobID : Option[String] =
      jobID match {
        case jobIDCompleteMessPattern(_)      => None
        case jobIDNoVersionPattern(mainJobID) => Some(mainJobID)
        case jobIDPattern(mainJobID, _, _)    => Some(mainJobID)
        case _ => None
      }

    foundMainJobID match {
      case None => Future.successful(Ok(Json.obj("exists" -> true)))
      case Some(mainJobID) =>
        val jobIDSearch = mainJobID + "(_[0-9]{1,3})?"
        Logger.info("Old job ID: " + mainJobID + " Current job ID: " + jobID + " Searching for: " + jobIDSearch)
        findJobs(BSONDocument(Job.JOBID -> BSONDocument("$regex" -> jobIDSearch))).map{ jobs =>
          if (jobs.isEmpty) {
            Logger.info("Found no such jobs.")
            Ok(Json.obj("exists" -> false))
          } else {
            Logger.info("Found " + jobs.length + " Jobs: " + jobs.map(_.jobID).mkString(","))
            if (resubmit) {
              val jobVersions = jobs.map{ job =>
                Logger.info("jobID to match: " + job.jobID)
                job.jobID match {
                  case jobIDNoVersionPattern(_) => 0
                  case jobIDPattern(_, _, v)    => if(v.isEmpty) -1 else Integer.parseInt(v)
                  case _ => 0
                }
              }
              val version : Int = jobVersions.max + 1
              //Logger.info("Resubmitting job ID version: " + version + " for " + mainJobID)
              Ok(Json.obj("exists" -> true, "version" -> version, "suggested" -> (mainJobID + "_" + version)))
            } else {
              //Logger.info("Main Job ID:" + mainJobID)
              Ok(Json.obj("exists" -> true))
            }
          }
        }

    }
  }
}
