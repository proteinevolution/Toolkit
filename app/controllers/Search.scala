package controllers

import models.database.jobs.{Job, JobHash}
import play.Logger
import models.Constants
import play.api.cache._
import play.api.libs.json.Json
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
final class Search @Inject()(@NamedCache("userCache") implicit val userCache: CacheApi,
                             implicit val locationProvider: LocationProvider,
                             val reactiveMongoApi: ReactiveMongoApi,
                             toolFactory: ToolFactory,
                             val jobDao: JobDAO)
    extends Controller
    with Constants
    with ReactiveMongoComponents
    with UserSessions
    with CommonModule {

  def getToolList: Action[AnyContent] = Action {
    Ok(Json.toJson(toolFactory.values.values.map(a => Json.obj("long" -> a.toolNameLong, "short" -> a.toolNameShort))))
  }

  def autoComplete(queryString: String): Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      val tools: List[models.tools.Tool] = toolFactory.values.values
        .filter(t => queryString.toLowerCase.r.findFirstIn(t.toolNameLong.toLowerCase()).isDefined)
        .toList
      Logger.info("user is looking for: " + queryString + " Found Tool: " + tools.map(_.toolNameShort).mkString(", "))
      // Find out if the user looks for a certain tool or for a jobID
      if (tools.isEmpty) {
        // Grab Job ID auto completions
        findJobs(BSONDocument(Job.JOBID -> BSONDocument("$regex" -> queryString))).map { jobs =>
          val jobsFiltered = jobs.filter(job => job.ownerID.contains(user.userID) && job.deletion.isEmpty)
          Ok(Json.toJson(jobsFiltered.map(_.cleaned())))
        }
      } else {
        // Find the Jobs with the matching tool
        findJobs(
          BSONDocument(Job.OWNERID -> user.userID, Job.TOOL -> BSONDocument("$in" -> tools.map(_.toolNameShort))))
          .map { jobs =>
            jobs.map(_.cleaned())
          }
          .map(jobJs => Ok(Json.toJson(jobJs)))
      }
    }
  }

  def existsTool(queryString: String): Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      val toolOpt: Option[models.tools.Tool] = toolFactory.values.values.find(_.isToolName(queryString))
      toolOpt match {
        case Some(_) => Future.successful(Ok(Json.toJson(true)))
        case None    => Future.successful(NotFound)
      }
    }
  }

  def get: Action[AnyContent] = Action.async { implicit request =>
    // Retrieve the jobs from the DB
    getUser.flatMap { user =>
      findJobs(BSONDocument(Job.OWNERID -> user.userID, Job.DELETION -> BSONDocument("$exists" -> false))).map {
        jobs =>
          Ok(Json.toJson(jobs.map(_.jobManagerJob())))
      }
    }
  }

  /**
    * Returns a json object containing both the last updated job and the most recent total number of jobs.
    *
    * @return
    */
  def getIndexPageInfo: Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      findSortedJob(
        BSONDocument(BSONDocument(Job.DELETION -> BSONDocument("$exists" -> false)),
                     BSONDocument(Job.OWNERID  -> user.userID)),
        BSONDocument(Job.DATEUPDATED -> -1)
      ).map { lastJob =>
        Ok(Json.obj("lastJob" -> lastJob.map(_.cleaned())))
      }
    }
  }

  /**
    * Looks for a jobID in the DB and checks if it is in use
    * if resubmit is true, the return object will also include the highest version jobID
    * @param jobID
    * @param resubmit
    * @return
    */
  def checkJobID(jobID: String, resubmit: Boolean = false): Action[AnyContent] = Action.async {
    val jobIDNoVersionPattern = "([0-9a-zA-Z_]{3,96})".r
    val jobVersionPattern     = "(_([0-9]{1,3}))".r
    val jobIDPattern          = (jobIDNoVersionPattern.regex + jobVersionPattern.regex).r
    val foundMainJobID: Option[String] =
      jobID match {
        case jobIDPattern(mainJobID, _, _)    => Some(mainJobID)
        case jobIDNoVersionPattern(mainJobID) => Some(mainJobID)
        case _                                => None
      }

    foundMainJobID match {
      case None => Future.successful(Ok(Json.obj("exists" -> true)))
      case Some(mainJobID) =>
        val jobIDSearch = mainJobID + "(_[0-9]{1,3})?"
        Logger.info("Old job ID: " + mainJobID + " Current job ID: " + jobID + " Searching for: " + jobIDSearch)
        findJobs(BSONDocument(Job.JOBID -> BSONDocument("$regex" -> jobIDSearch))).map { jobs =>
          if (jobs.isEmpty) {
            Logger.info("Found no such jobs.")
            Ok(Json.obj("exists" -> false))
          } else {
            if (resubmit) {
              Logger.info("Found " + jobs.length + " Jobs: " + jobs.map(_.jobID).mkString(","))
              val jobVersions = jobs.map { job =>
                Logger.info("jobID to match: " + job.jobID)
                job.jobID match {
                  case jobIDPattern(_, _, v) => if (v.isEmpty) { -1 } else { Integer.parseInt(v) }
                  case _                     => 0
                }
              }
              val version: Int = jobVersions.max + 1
              //Logger.info("Resubmitting job ID version: " + version + " for " + mainJobID)
              Ok(Json.obj("exists" -> true, "version" -> version, "suggested" -> (mainJobID + "_" + version)))
            } else {
              Ok(Json.obj("exists" -> jobs.map(_.jobID).contains(jobID)))
            }
          }
        }

    }
  }
}
