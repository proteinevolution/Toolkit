package de.proteinevolution.search.services

import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.models.database.jobs.Job
import de.proteinevolution.models.database.users.User
import de.proteinevolution.services.ToolConfig
import javax.inject.{ Inject, Singleton }
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class SearchService @Inject()(
    jobDao: JobDao,
    toolConfig: ToolConfig
)(implicit ec: ExecutionContext) {

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
    val tools: List[de.proteinevolution.models.Tool] = toolConfig.values.values
      .filter(t => queryString.toLowerCase.r.findFirstIn(t.toolNameLong.toLowerCase()).isDefined)
      .filterNot(_.toolNameShort == "hhpred_manual")
      .toList
    if (tools.isEmpty) {
      (for {
        jobs <- OptionT.liftF(jobDao.findJobs(BSONDocument(Job.JOBID -> BSONDocument("$regex" -> queryString))))
      } yield {
        val jobsFiltered = jobs.filter(job => job.ownerID.contains(user.userID))
        jobsFiltered
      }).flatMapF { jobs =>
        if (jobs.isEmpty) {
          OptionT(jobDao.findJob(BSONDocument(Job.JOBID -> queryString))).map(_ :: Nil).value
        } else {
          Future.successful(Some(jobs))
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
