package de.proteinevolution.jobs.services

import better.files._
import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.common.models.database.jobs.JobState.Done
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.Job
import javax.inject.{ Inject, Singleton }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JobHashService @Inject()(
    jobDao: JobDao,
    constants: ConstantsV2,
    hashService: GeneralHashService
)(implicit ec: ExecutionContext)
    extends JobFolderValidation {

  def checkHash(jobID: String): OptionT[Future, Job] = {
    for {
      job      <- OptionT(jobDao.findJob(jobID))
      list     <- OptionT.liftF(listSameJobsSorted(job))
      filtered <- OptionT.fromOption[Future](list.find(filterJobs(job, _)))
    } yield filtered
  }

  private[this] def listSameJobsSorted(job: Job): Future[List[Job]] =
    jobDao.findAndSortJobs(hashService.generateJobHash(job, params(job.jobID)))

  private[this] def filterJobs(job: Job, j: Job): Boolean =
    (j.isPublic || j.ownerID == job.ownerID) && j.status == Done && resultsExist(j.jobID, constants)

  private[this] def params(jobID: String): Map[String, String] = {
    (constants.jobPath / jobID / constants.serializedParam).readDeserialized[Map[String, String]]
  }

}
