package controllers

import models.database.jobs.Job
import play.Logger
import models.Constants
import play.api.cache._
import play.api.libs.json.Json
import javax.inject.{ Inject, Singleton }

import play.modules.reactivemongo.{ ReactiveMongoApi, ReactiveMongoComponents }
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import models.search.JobDAO
import models.tools.ToolFactory
import modules.LocationProvider
import modules.db.MongoStore
import play.api.mvc.{ Action, AnyContent, Controller }

import scala.concurrent.Future
import scala.language.postfixOps

@Singleton
final class Search @Inject()(@NamedCache("userCache") implicit val userCache: CacheApi,
                             implicit val locationProvider: LocationProvider,
                             userSessions: UserSessions,
                             val reactiveMongoApi: ReactiveMongoApi,
                             mongoStore: MongoStore,
                             toolFactory: ToolFactory,
                             val jobDao: JobDAO,
                             constants: Constants)
    extends Controller
    with ReactiveMongoComponents
    with Common {

  def getToolList: Action[AnyContent] = Action {
    Ok(
      Json.toJson(
        toolFactory.values.values
          .filterNot(_.toolNameShort == "hhpred_manual")
          .map(a => Json.obj("long" -> a.toolNameLong, "short" -> a.toolNameShort))
      )
    )
  }

  /**
    * fetches data for a given query
    *
    * if no tool is found for a given query,
    * it looks for jobs which belong to the current user.
    * only jobIDs that belong to the user are autocompleted
    *
    * @param queryString_
    * @return
    */
  def autoComplete(queryString_ : String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      val queryString = queryString_.trim()
      val tools: List[models.tools.Tool] = toolFactory.values.values
        .filter(t => queryString.toLowerCase.r.findFirstIn(t.toolNameLong.toLowerCase()).isDefined)
        .filter(tool => tool.toolNameShort != "hhpred_manual")
        .toList
      // Find out if the user looks for a certain tool or for a jobID
      if (tools.isEmpty) {
        // Grab Job ID auto completions
        mongoStore.findJobs(BSONDocument(Job.JOBID -> BSONDocument("$regex" -> queryString))).flatMap { jobs =>
          val jobsFiltered = jobs.filter(job => job.ownerID.contains(user.userID) && job.deletion.isEmpty)
          if (jobsFiltered.isEmpty) {
            mongoStore
              .findJob(BSONDocument(Job.JOBID -> queryString))
              .map(x => Ok(Json.toJson(List(x.map(_.cleaned())))))
          } else {
            Future.successful(Ok(Json.toJson(jobsFiltered.map(_.cleaned()))))
          }
        }
      } else {
        // Find the Jobs with the matching tool
        mongoStore
          .findJobs(
            BSONDocument(Job.OWNERID -> user.userID, Job.TOOL -> BSONDocument("$in" -> tools.map(_.toolNameShort)))
          )
          .map { jobs =>
            jobs.map(_.cleaned())
          }
          .map(jobJs => Ok(Json.toJson(jobJs)))
      }
    }
  }

  def existsTool(queryString: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      val toolOpt: Option[models.tools.Tool] = toolFactory.values.values.find(_.isToolName(queryString))
      toolOpt match {
        case Some(_) => Future.successful(Ok(Json.toJson(true)))
        case None    => Future.successful(NotFound)
      }
    }
  }

  def get: Action[AnyContent] = Action.async { implicit request =>
    // Retrieve the jobs from the DB
    userSessions.getUser.flatMap { user =>
      mongoStore
        .findJobs(BSONDocument(Job.OWNERID -> user.userID, Job.DELETION -> BSONDocument("$exists" -> false)))
        .map { jobs =>
          NoCache(Ok(Json.toJson(jobs.map(_.jobManagerJob()))))
        }
    }
  }

  /**
    * Returns a json object containing both the last updated job and the most recent total number of jobs.
    *
    * @return
    */
  def getIndexPageInfo: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      mongoStore
        .findSortedJob(
          BSONDocument(BSONDocument(Job.DELETION -> BSONDocument("$exists" -> false)),
                       BSONDocument(Job.OWNERID  -> user.userID)),
          BSONDocument(Job.DATEUPDATED -> -1)
        )
        .map { lastJob =>
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
        mongoStore.findJobs(BSONDocument(Job.JOBID -> BSONDocument("$regex" -> jobIDSearch))).map { jobs =>
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
