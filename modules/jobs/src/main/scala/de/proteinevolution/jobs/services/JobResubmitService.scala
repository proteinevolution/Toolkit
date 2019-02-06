package de.proteinevolution.jobs.services

import de.proteinevolution.base.helpers.ToolkitTypes
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.{ Job, ResubmitData }
import de.proteinevolution.models.ConstantsV2
import javax.inject.{ Inject, Singleton }
import play.api.Logging
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JobResubmitService @Inject()(constants: ConstantsV2, jobDao: JobDao)(implicit ec: ExecutionContext)
    extends ToolkitTypes
    with Logging {

  def resubmit(newJobId: String, resubmitForJobId: Option[String]): Future[ResubmitData] = {
    generateParentJobId((newJobId, resubmitForJobId)) match {
      case Some(parentJobId) =>
        findJobs(parentJobId).map { jobs =>
          generateResubmitData(jobs, newJobId, resubmitForJobId, parentJobId)
        }
      case None =>
        logger.info(
          s"invalid jobID: ${newJobId.trim}${resubmitForJobId.map(a => s" Resubmit jobID: $a").getOrElse("")}"
        )
        fuccess(ResubmitData(exists = true, None, None))
    }
  }

  private def generateParentJobId(ids: (String, Option[String])): Option[String] = {
    // Parse the jobID of the job (it can look like this: 1234XYtz, 1263412, 1252rttr_1, 1244124_12)
    ids match {
      case (newJobID, resubmitForJobID) =>
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
    }
  }

  private def findJobs(parentJobId: String): Future[List[Job]] = {
    jobDao.findJobs(
      BSONDocument(
        Job.JOBID -> BSONDocument("$regex" -> s"$parentJobId(${constants.jobIDVersioningCharacter}[0-9]{1,3})?")
      )
    )
  }

  private def generateResubmitData(
      jobs: List[Job],
      newJobId: String,
      resubmitForJobId: Option[String],
      parentJobId: String
  ): ResubmitData = {
    if (!jobs.map(_.jobID).contains(newJobId)) {
      ResubmitData(exists = false, None, None)
    } else {
      if (resubmitForJobId.nonEmpty) {
        val version = generateJobVersion(jobs)
        ResubmitData(
          exists = true,
          version = Some(version),
          suggested = Some(s"$parentJobId${constants.jobIDVersioningCharacter}$version")
        )
      } else {
        ResubmitData(exists = true, None, None)
      }
    }
  }

  private def generateJobVersion(jobs: List[Job]): Int = {
    val jobVersions = jobs.map { job =>
      job.jobID match {
        case constants.jobIDPattern(_, v) => v.toInt
        case _                            => 0
      }
    }
    val version: Int = 1 + jobVersions.sorted.fold(1)(
      (versionBeforeGap, biggerVersion) =>
        if (versionBeforeGap + 1 >= biggerVersion) biggerVersion else versionBeforeGap
    )
    version
  }

}
