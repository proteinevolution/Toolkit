package de.proteinevolution.search.services

import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.base.helpers.ToolkitTypes._
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.jobs.services.JobFolderValidation
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.common.models.database.users.User
import de.proteinevolution.tools.{ Tool, ToolConfig }
import javax.inject.{ Inject, Singleton }
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class SearchService @Inject()(
    jobDao: JobDao,
    constants: ConstantsV2,
    toolConfig: ToolConfig
)(implicit ec: ExecutionContext)
    extends JobFolderValidation {

  def recentJob(user: User): Future[Option[Job]] = {
    jobDao.findSortedJob(
      BSONDocument(
        BSONDocument(Job.DELETION -> BSONDocument("$exists" -> false)),
        BSONDocument(Job.OWNERID  -> user.userID)
      ),
      BSONDocument(Job.DATEUPDATED -> -1)
    )
  }

  def autoComplete(user: User, queryString_ : String): OptionT[Future, List[Job]] = {
    val queryString = queryString_.trim()
    val tools: List[Tool] = toolConfig.values.values
      .filter(t => queryString.toLowerCase.r.findFirstIn(t.toolNameLong.toLowerCase()).isDefined)
      .filterNot(_.toolNameShort == "hhpred_manual")
      .toList
    if (tools.isEmpty) {
      (for {
        jobs     <- OptionT.liftF(jobDao.findJobs(BSONDocument(Job.JOBID -> BSONDocument("$regex" -> queryString))))
        filtered <- OptionT.pure[Future](jobs.filter(job => job.ownerID.contains(user.userID)))
      } yield {
        filtered
      }).flatMapF { jobs =>
        if (jobs.isEmpty) {
          OptionT(jobDao.findJob(BSONDocument(Job.JOBID -> queryString)))
            .filter(job => resultsExist(job.jobID, constants))
            .map(_ :: Nil)
            .value
        } else {
          fuccess(Some(jobs.filter(job => resultsExist(job.jobID, constants))))
        }
      }
    } else {
      OptionT.liftF(
        jobDao.findJobs(
          BSONDocument(Job.OWNERID -> user.userID, Job.TOOL -> BSONDocument("$in" -> tools.map(_.toolNameShort)))
        )
      )
    }
  }

}
