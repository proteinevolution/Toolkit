package actors

import javax.inject.{Inject, Singleton}

import actors.JobActor.JobData
import actors.Master.{CreateJob, DeleteJob, JobOperation, WorkerDoneWithJob}
import actors.UserManager.{UserConnect, UserDisconnect}
import akka.actor.{Actor, ActorRef, Props}
import akka.event.LoggingReceive
import models.database.User
import play.api.cache.{CacheApi, NamedCache}
import reactivemongo.bson.BSONObjectID

import scala.collection.mutable

/**
  * Created by lzimmermann on 08.12.16.
  */
@Singleton
class Master @Inject() (@NamedCache("jobActorCache") val jobActorCache: CacheApi,
                        jobActorFactory : JobActor.Factory) extends Actor {

  // TODO Get the number of JobActors per master from Configuration
  private val availJobActors : mutable.Queue[ActorRef] =
    mutable.Queue.fill(100)(context.actorOf(Props(jobActorFactory.apply)))

  private val pendingWork = mutable.Queue[JobOperation]()

  private val jobs = mutable.Map[String, ActorRef]()

  private val deletedJobs = mutable.Set[String]()

  private val userWebSockets = mutable.Map[BSONObjectID, ActorRef]()



  def receive = LoggingReceive {

    // Master registers user
    case UserConnect(userID) =>
      userWebSockets.put(userID, sender())

    // Pending Jobs
    case c@CreateJob(jobID, userWithWS, params) =>

      // Assign if a worker is available, otherwise remember work
      if(availJobActors.isEmpty) {
        pendingWork.enqueue(c)
      } else {

        val nextWorker = availJobActors.dequeue()
        jobs.put(jobID, nextWorker )
        jobActorCache.set(jobID, nextWorker)
        nextWorker ! c.copy( userWithWS=
          userWithWS match {
            case (user, None) =>
              val userID = user.userID
              if(userWebSockets.contains(userID)) (user, Some(userWebSockets(userID))) else (user, None)
            case (user, Some(actorRef)) => (user, Some(actorRef))
          }
        )
      }

    case d@DeleteJob(jobID) =>
      if(jobs.contains(jobID)) jobs(jobID) ! d  else deletedJobs.add(jobID)
      jobActorCache.remove(jobID)


     // Worker notified that no more work has to be done for the current Job
    case WorkerDoneWithJob(jobID) =>

      val assignee = sender()
      jobs.remove(jobID)
      // Dequeue until next undeltedJobHasBeen Found
      pendingWork.dequeueFirst(op => !deletedJobs.contains(op.jobID)) match {

        case Some(op) =>
          jobs.put(op.jobID, assignee)
          assignee ! op

        // No More things to be processed
        case None =>
          availJobActors.enqueue(assignee)
      }
  }
}

object Master {

  /*
       JobOperations that can be initiated
   */
  abstract class JobOperation(val jobID : String)

  // The initiating user is eiter identified via the sessionID or directly via the WebSocket Actor
  case class CreateJob(override val jobID: String,                     // The ID of the Job
                       userWithWS: (User, Option[ActorRef]),         //
                       jobData: JobData) extends JobOperation(jobID)

  case class DeleteJob(override val jobID: String) extends JobOperation(jobID)


  /* Status Messages coming from the Workers */
  sealed trait WorkerMessage
  case class WorkerDoneWithJob(jobID: String)  // Worker denotes itself as being available again, can be sent new work

}





