package services

import javax.inject._

import actors.JobActor.Delete
import akka.actor.ActorSystem
import models.database.jobs.{ DeletedJob, Job }
import models.database.users.User
import models.job.JobActorAccess
import models.search.JobDAO
import modules.db.MongoStore
import java.time.ZonedDateTime
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{ BSONDateTime, BSONDocument }
import better.files._
import models.Constants

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
trait SweepJobs {
  def sweep(): Unit
}

/**
  * This class takes care of the job deletion routine.
  * An asynchronous task is executed on the Toolkit startup
  * and then called every deletionCycle minutes.
  * A list of all jobs that were delete is written to a file
  * and to the deletedCollection in mongoDB
  *
  * This routine includes:
  *
  * 1.  finding jobs of non registered users
  *     which are older than deletionThreshold and
  *     jobs of registered users which are older than
  *     userThresholdRegistered
  * 2.  finding the corresponding user to the job
  * 3.  pull job from watchlist, delete from ElasticSearch
  * 4.  if jobs has no ownerID or user is not found:
  *     delete job from ElasticSearch
  * 5.  delete job folder, resultCollection
  */
@Singleton
class SweepJobsImpl @Inject()(appLifecycle: ApplicationLifecycle,
                              actorSystem: ActorSystem,
                              val reactiveMongoApi: ReactiveMongoApi,
                              mongoStore: MongoStore,
                              val jobDao: JobDAO,
                              jobActorAccess: JobActorAccess,
                              constants: Constants)
    extends SweepJobs {

  override def sweep(): Unit = actorSystem.scheduler.schedule(0 seconds, constants.deletionCycle hours) {
    deleteJobsPermanently()
  }

  /**
    * this method finds jobs of non registered users
    * which are older than deletionThreshold and
    * jobs of registered users which are older than
    * userThresholdRegistered
    * then it finds the corresponding user to a job
    * if the job has an owner ID:
    * pull job from watchlist
    * then delete from ElasticSearch and delete it
    * from mongoDB and remove the job folder
    *
    * @return
    */
  def deleteJobsPermanently(): Unit = {

    Logger.info("Sweep Jobs routine active!")
    /*
     * deletes jobs are older than a given number of days
     * ('deletionThresholdLoggedIn' for registered users and  'deletionThreshold' for others)
     * and informs all watching users about it in behalf of the job maintenance routine
     *
     */
    mongoStore
      .findJobs(
        BSONDocument(
          Job.DATECREATED -> BSONDocument(
            "$lt" -> BSONDateTime(ZonedDateTime.now.minusDays(constants.deletionThreshold).toInstant.toEpochMilli)
          )
        )
      )
      .map { jobList =>
        jobList.map { job =>
          job.ownerID match {
            case Some(id) =>
              mongoStore.findUser(BSONDocument(User.IDDB -> BSONDocument("$eq" -> id))).map {
                case Some(user) =>
                  val storageTime = ZonedDateTime.now.minusDays(if (user.accountType == -1) {
                    constants.deletionThreshold
                  } else constants.deletionThresholdRegistered)
                  mongoStore
                    .findJob(
                      BSONDocument(
                        "$and" -> List(
                          BSONDocument(Job.JOBID -> job.jobID),
                          BSONDocument(
                            Job.DATECREATED -> BSONDocument("$lt" -> BSONDateTime(storageTime.toInstant.toEpochMilli))
                          )
                        )
                      )
                    )
                    .map {
                      case Some(deletedJob) =>
                        Logger.info("Deleting job: " + deletedJob.jobID)
                        // Message user clients to remove the job from their watchlist
                        jobActorAccess.sendToJobActor(deletedJob.jobID,
                                                      Delete(deletedJob.jobID, deletedJob.ownerID.get, false))
                        this.deleteJobPermanently(job)
                        this.writeJob(job.jobID)
                      case None =>
                    }
                case None =>
                  Logger.info("User not found: " + id.stringify + s". Job ${job.jobID} is directely deleted.")
                  jobDao.deleteJob(job.mainID.stringify)
                  this.writeJob(job.jobID)
                  this.deleteJobPermanently(job)
              }
            case None =>
              Logger.info("Job " + job.jobID + " has no owner ID. It is directely deleted")
              this.deleteJobPermanently(job)
              this.writeJob(job.jobID)
              jobDao.deleteJob(job.mainID.stringify)
          }
        }
      }

    /*
     * deletes all jobs that are marked for deletion with
     * (deletion.flag == 1)
     * the duration of keeping the job is dependent on whether the user is a registered user
     */
    mongoStore.findJobs(BSONDocument(BSONDocument("deletion.flag" -> BSONDocument("$eq" -> 1)))).map { jobList =>
      jobList.foreach { job =>
        this.deleteJobPermanently(job)
        this.writeJob(job.jobID)
      }
    }
  }

  /**
    * deletes job from job path, resultCollection, jobCollection
    * @param job
    */
  def deleteJobPermanently(job: Job): Unit = {
    Logger.info("Deleting jobFolder" + { constants.jobPath } + "/" + job.jobID)
    s"${constants.jobPath}${job.jobID}".toFile.delete(true)
    Logger.info("Removing Job " + job.jobID + " from mongo DB")
    mongoStore.removeJob(BSONDocument(Job.JOBID -> job.jobID))
  }

  /**
    * writes the jobID and deletion date to a file in deletionLogPath
    * and to the deletionCollection
    *
    * @param jobID
    */
  def writeJob(jobID: String): Unit = {
    constants.deletionLogPath.toFile.appendLine(jobID + "\t" + ZonedDateTime.now.toString())
    mongoStore.addDeletedJob(DeletedJob(jobID, ZonedDateTime.now))

  }

  // TODO: is not only called on startup but also called on application stop
  sweep()
}
