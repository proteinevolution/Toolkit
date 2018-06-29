package controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.models.database.jobs.Job
import play.api.libs.json.Json
import javax.inject.{ Inject, Singleton }
import de.proteinevolution.models.ConstantsV2
import reactivemongo.bson.BSONDocument
import de.proteinevolution.jobs.dao.JobDao
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class Search @Inject()(
    jobDao: JobDao,
    constants: ConstantsV2,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  private val logger = Logger(this.getClass)

  /**
   * Looks for a jobID in the DB and checks if it is in use
   * if resubmit is true, the return object will also include the highest version jobID
   */
  def checkJobID(newJobID: String, resubmitForJobID: Option[String]): Action[AnyContent] = Action.async {
    // Parse the jobID of the job (it can look like this: 1234XYtz, 1263412, 1252rttr_1, 1244124_12)
    val parentJobID: Option[String] =
      newJobID match {
        case constants.jobIDPattern(mainJobID, _) =>
          // Check if the main part of the new jobID matches with the (main part) of the oldJobID
          resubmitForJobID match {
            case Some(constants.jobIDPattern(oldJobID, _)) => if (mainJobID == oldJobID) Some(mainJobID) else None
            case Some(constants.jobIDNoVersionPattern(oldJobID)) =>
              if (mainJobID == oldJobID) Some(mainJobID) else None
            case _ => None
          }
        case constants.jobIDNoVersionPattern(mainJobID) => Some(mainJobID)
        case _                                          => None
      }

    parentJobID match {
      case None =>
        logger.info(
          s"[Search.checkJobID] invalid jobID: ${newJobID.trim}${resubmitForJobID.map(a => s" Resubmit jobID: $a").getOrElse("")}"
        )
        Future.successful(Ok(Json.obj("exists" -> true)))
      case Some(mainJobID) =>
        val jobIDSearch = s"$mainJobID(${constants.jobIDVersioningCharacter}[0-9]{1,3})?"
        logger.info(
          s"[Search.checkJobID] JobID suggestions:${resubmitForJobID.map(a => s"\nOld jobID: $a").getOrElse("")} \nMain part of the jobID: $mainJobID \nCurrent job ID: $newJobID \nSearching for: $jobIDSearch"
        )
        jobDao.findJobs(BSONDocument(Job.JOBID -> BSONDocument("$regex" -> jobIDSearch))).map { jobs =>
          if (!jobs.map(_.jobID).contains(newJobID)) {
            logger.info(s"[Search.checkJobID] Found no jobs for the jobID $newJobID.")
            Ok(Json.obj("exists" -> false))
          } else {
            if (resubmitForJobID.nonEmpty) {
              // Check if there is a versioned job already - if so, take the highest version and add one
              logger.info(s"[Search.checkJobID] Found ${jobs.length} Jobs: ${jobs.map(_.jobID).mkString(",")}")
              val jobVersions = jobs.map { job =>
                job.jobID match {
                  case constants.jobIDPattern(_, v) =>
                    v.toInt
                  case _ => 0
                }
              }
              val version: Int = 1 + jobVersions.sorted.fold(1)(
                (versionBeforeGap, biggerVersion) =>
                  if (versionBeforeGap + 1 >= biggerVersion) biggerVersion
                  else versionBeforeGap
              )
              logger.info(s"[Search.checkJobID] Resubmitting jobID version: $version for $mainJobID")
              Ok(
                Json.obj(
                  "exists"    -> true,
                  "version"   -> version,
                  "suggested" -> s"$mainJobID${constants.jobIDVersioningCharacter}$version"
                )
              )
            } else {
              // Just check if the jobID is taken, it is a regular job
              logger.info(s"[Search.checkJobID] Found a similiar job for $mainJobID")
              Ok(Json.obj("exists" -> true))
            }
          }
        }
    }
  }
}
