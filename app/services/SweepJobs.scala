package services

import javax.inject._

import actors.JobActor.Delete
import akka.actor.ActorSystem
import models.database.jobs.Job
import models.database.users.User
import models.job.JobActorAccess
import models.search.JobDAO
import modules.db.MongoStore
import org.joda.time.DateTime
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDateTime, BSONDocument}
import better.files._
import models.Constants
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
trait SweepJobs {
  def sweep(): Unit
}

@Singleton
class SweepJobsImpl @Inject() (appLifecycle: ApplicationLifecycle,
                               actorSystem: ActorSystem,
                                val reactiveMongoApi: ReactiveMongoApi,
                                mongoStore: MongoStore,
                                val jobDao: JobDAO,
                                jobActorAccess: JobActorAccess, constants: Constants) extends SweepJobs {

  override def sweep(): Unit = actorSystem.scheduler.schedule(0 seconds, 30 minutes) {
  deleteJobsPermanently()
  }

  /**
    * markes jobs as deleted and subsequently deletes them from dbs and harddisk
    *
    * @return
    */
  def deleteJobsPermanently() : Unit = {

    Logger.info("Sweep Jobs routine active!")
    /*
      * deletes jobs are older than a given number of days
      * ('deletionThresholdLoggedIn' for registered users and  'deletionThreshold' for others)
      * and informs all watching users about it in behalf of the job maintenance routine
      *
      */
    mongoStore.findJobs(BSONDocument(Job.DATECREATED -> BSONDocument("$lt" -> BSONDateTime(new DateTime().minusDays(constants.deletionThreshold).getMillis)))).map { jobList =>
      jobList.map { job =>
        job.ownerID match {
          case Some(id) =>
            mongoStore.findUser(BSONDocument(User.IDDB -> BSONDocument("$eq" -> id))).map {
              case Some(user) =>
                val storageTime = new DateTime().minusDays(if (user.accountType == -1) {constants.deletionThreshold} else constants.deletionThresholdRegistered)
                mongoStore.findJob(BSONDocument(
                  "$and" -> List(
                    BSONDocument(Job.JOBID -> job.jobID),
                    BSONDocument(Job.DATECREATED -> BSONDocument("$lt" -> BSONDateTime(storageTime.getMillis)))
                  )
                )).map {
                  case Some(deletedJob) =>
                    println(deletedJob.jobID)
                    // Message user clients to remove the job from their watchlist
                    jobActorAccess.sendToJobActor(deletedJob.jobID, Delete(deletedJob.jobID, deletedJob.ownerID.get, false))
                    this.deleteJobPermanently(job)
                  case None =>
                }
              case None =>
                Logger.info("User not found: " + id.stringify + s". Job ${job.jobID} is directely deleted.")
                jobDao.deleteJob(job.mainID.stringify)
                this.deleteJobPermanently(job)
            }
          case None =>
            Logger.info("Job " + job.jobID + " has no owner ID. It is directely deleted")
            this.deleteJobPermanently(job)
            jobDao.deleteJob(job.mainID.stringify)

        }
      }
    }

    /*
      * deletes all jobs that are marked for deletion with
      * (deletion.flag == 1)
      * the duration of keeping the job is dependent on whether the user is a registered user
      */
    println("Deleting jobs that user have requested for deletion in progress...")
    mongoStore.findJobs(BSONDocument(BSONDocument("deletion.flag" -> BSONDocument("$eq" -> 1)))).map { jobList =>
      jobList.foreach { job =>
        this.deleteJobPermanently(job)
      }
    }
  }

  /**
    * deletes the Job from disk.
    * Includes: remove job and result from mongoDB,
    * delete job folder
    *
    * @param job
    */
  def deleteJobPermanently(job: Job): Unit ={
    Logger.info("Deleting jobFolder" + {constants.jobPath}+ "/"+job.jobID)
    s"${constants.jobPath}${job.jobID}".toFile.delete(true)
    Logger.info("Removing Job "+job.jobID+" from mongo DB")
    mongoStore.removeJob(BSONDocument(Job.JOBID -> job.jobID))
  }

  // Called when this singleton is constructed
  sweep()

}