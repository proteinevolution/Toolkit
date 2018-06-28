package de.proteinevolution.jobs.services

import java.io.{ FileInputStream, ObjectInputStream }

import better.files._
import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.db.MongoStore
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs.Job
import de.proteinevolution.models.database.jobs.JobState.Done
import de.proteinevolution.models.search.JobDAO
import de.proteinevolution.tel.env.Env
import javax.inject.{ Inject, Singleton }
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JobHashService @Inject()(
    env: Env,
    mongoStore: MongoStore,
    constants: ConstantsV2,
    jobDao: JobDAO
)(implicit ec: ExecutionContext) {

  def checkHash(jobID: String): OptionT[Future, Job] = {
    for {
      job <- OptionT(mongoStore.findJob(BSONDocument(Job.JOBID -> jobID)))
      list <- OptionT.liftF(
        mongoStore.findAndSortJobs(
          BSONDocument(Job.HASH        -> jobDao.generateJobHash(job, params(jobID), env)),
          BSONDocument(Job.DATECREATED -> -1)
        )
      )
    } yield {
      list.find(j => (j.isPublic || j.ownerID == job.ownerID) && j.status == Done)
    }
  }.flatMap(OptionT.fromOption(_))

  private def params(jobID: String): Map[String, String] = {
    val ois = new ObjectInputStream(
      new FileInputStream((constants.jobPath / jobID / constants.serializedParam).pathAsString)
    )
    val x = ois.readObject().asInstanceOf[Map[String, String]]
    ois.close()
    x
  }

}
