package master

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import models.database.DBJobRef
import models.distributed.FrontendMasterProtocol._
import models.distributed.{FrontendMasterProtocol, MasterWorkerProtocol, Work}
import models.jobs.UserJob
import models.misc.RandomString
import play.api.Logger

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer


object Master {

  def props : Props = Props(classOf[Master])


  private sealed trait WorkerStatus
  private case object Idle extends WorkerStatus
  private case class Busy(workID: String) extends WorkerStatus
  private case class WorkerState(ref: ActorRef, status: WorkerStatus)

  private case object CleanupTick

}

class Master extends Actor with ActorLogging {

  import Master._
  import models.distributed.WorkState
  import models.distributed.WorkState._

  val jobIDRegex = "[0-9a-zA-Z_-]+"

  val mediator = DistributedPubSub(context.system).mediator


  // Counter for WorkID and JobID
  var workIDCounter : Long = 0  // TODO Maybe replace by Config
  var mainIDCounter : Long = 0



  // workState is event sourced
  private var workState = WorkState.empty

  // workers are not event sourced //TODO Master currently spawns workers by himself. Shouldn't be the case
  private val workers = scala.collection.mutable.Map[String, WorkerState](
    "worker1" -> WorkerState(context.actorOf(Worker.props(self, "worker1")), Idle),
    "worker2" -> WorkerState(context.actorOf(Worker.props(self, "worker2")), Idle),
    "worker3" -> WorkerState(context.actorOf(Worker.props(self, "worker3")), Idle),
    "worker4" -> WorkerState(context.actorOf(Worker.props(self, "worker4")), Idle)
  )


  val userWebSockets = scala.collection.mutable.Map[String, ActorRef]()

  // Maps Session ID of user to all known jobs of the corresponsing user
  val userJobs = new mutable.HashMap[String, mutable.HashMap[String, UserJob]]

  // Maps Session ID to something that has something to do with the DB // TODO Reactivate
  val databaseMapping = new collection.mutable.HashMap[String, ArrayBuffer[DBJobRef]]


  /*
    Methods of the Master
   */

  /* TODO Only for persistent actor
  // persistenceId must include cluster role to support multiple masters
  // TODO Probably does not work with current config
  override def persistenceId: String = Cluster(context.system).selfRoles.find(_.startsWith("backend-")) match {

    case Some(role) => role + "-master"
    case None       => "master"
  }*/

  // Informs all Idle Workers that there is Work to be done
  def notifyWorkers(): Unit =

    if (workState.hasWork) {

      // Send Idle Workers that work is Ready
      workers.foreach {
        case (_, WorkerState(ref, Idle)) => ref ! MasterWorkerProtocol.WorkIsReady
        case _                           => // worker is busy, no need to send Idle
      }
    }

  // Puts a worker to Idle State
  def changeWorkerToIdle(workerID: String, workID: String): Unit = {

    workers.get(workerID) match {

      case Some(s@WorkerState(_, Busy(_))) =>

        workers.update(workerID, s.copy(status = Idle))

      case _ => // ok, might happen after standby recovery, worker state is not persisted
    }
  }

  // Fetch the Job that belongs to a Request
  def evalJob(sender : ActorRef,
              newJob : Boolean,
              sessionID : String,
              toolname : String,
              jobID : String) : Option[UserJob] = {

    val userJobIDs  = userJobs(sessionID).keySet

    if(newJob) {
      if(userJobIDs.contains(jobID)) {

        Logger.info("Master says that jobID is already in use.")
        sender ! FrontendMasterProtocol.JobIDAlreadyInUse
        None
      } else {
        // This is an new Job
        val newJobID = if(jobID.matches(jobIDRegex)) jobID else RandomString.randomNumString(7)

        val userJob = UserJob(mediator,sessionID, toolname, mainIDCounter, newJobID, start = false)
        mainIDCounter += 1

        userJobs(sessionID).put(newJobID, userJob)
        sender ! Accepted
        Some(userJob)
      }
    } else {
      if(!userJobIDs.contains(jobID)) {
        sender ! JobUnknown
        None
      }
      else {
        // Just get the UserJob From the Map
        Some(userJobs(sessionID)(jobID))
      }
    }
  }


  /*
    * Generates a new JobID for the particular job
    *
    * @param job_id_o selected job_id
    * @return
    */
  /*
  def checkOrGenerateJobID(job_id_o: Option[String]): String = {

    val fakeUserID = 12345 // TODO UserID not currently implemented

    // Determine the Job ID for the Job that was submitted
    var job_id: String = job_id_o.getOrElse(RandomString.randomNumString(7))
    while (jobDB.get(fakeUserID, job_id).nonEmpty) {
      job_id = RandomString.randomNumString(7)
    }
    job_id
  }


  def addJob(sender: ActorRef, sessionID: String, dbJob: DBJob) = {

    val job = UserJob(self, dbJob.toolname, dbJob.job_id, dbJob.user_id, dbJob.job_state, startImmediate = true)
    val jobRef = jobRefDB.update(dbJob, sessionID)

    userJobs(sessionID).append(job)
    databaseMapping(sessionID).append(jobRef)

    sender ! UpdateJob(job)
  } */


  //ClusterClientReceptionist(context.system).registerService(self)


  // workers state is not event sourced TODO Currently the workers are injected into the Master
  //private var workers = Map[String, WorkerState]()

  //val cleanupTask = context.system.scheduler.schedule(workTimeout / 2, workTimeout / 2,
  //  self, CleanupTick)

  //override def postStop(): Unit = cleanupTask.cancel()

  // TODO Only for persistent actor
  /*
  override def receiveRecover: Receive = {
    case event: WorkDomainEvent =>
      // only update current state by applying the event, no side effects
      workState = workState.updated(event)
      log.info("Replayed {}", event.getClass.getSimpleName)
  } */


  override def receive: Receive = {

    // A new Sesssion has registered at the Master
    case FrontendMasterProtocol.SubscribeToMaster(sessionID) =>

      // TODO This map might be superfluous
      userWebSockets.put(sessionID, sender())
      context watch sender()
      Logger.info("Master: User with SessionID " + sessionID + "has subscribed successfully")

      // Register the sender at the PubSubMediator
      mediator ! Subscribe("SESSION_" + sessionID, sender())



      /* TODO Database currently disabled// TODO This will be rather slow, we shouldn't do that
      // Load All Jobs from the Database
      for (jobRef <- jobRefDB.get(sessionID)) {
        val dbJob_o = jobDB.get(jobRef.main_id)
        dbJob_o match {
          // job with the main_id exists, add the job
          case Some(dbJob) => addJob(sender(), sessionID, dbJob)
          case None => jobRefDB.delete(jobRef)
        }
      } */


    case Terminated(user) => // TODO Remove user from map


      /* Master handles the different requests */

    // Prepare Requests
    case userRequest : UserRequestPrepare =>

      val sessionID = userRequest.sessionID
      val newJob = userRequest.newJob
      val toolname = userRequest.toolname
      val jobID = userRequest.jobID

      Logger.info("Master was asked to prepare a Job from SessionID " + sessionID)
      userJobs.getOrElseUpdate(sessionID, mutable.HashMap.empty[String, UserJob])

      if(!userWebSockets.contains(sessionID)) {
        Logger.info("Master does not know the SessionID " + sessionID)
        sender() ! SessionIDUnknown
      }
      else {

        // determine the UserJobObject
        val userJob : Option[UserJob] = evalJob(sender(), newJob, sessionID, toolname, jobID)

        // There is Work do be done
        if(userJob.nonEmpty) {

          val work = Work(workIDCounter.toString, userRequest, userJob.get)
          workIDCounter += 1
          val event = WorkAccepted(work)
          workState = workState.updated(event)
          notifyWorkers()
        }
      }

    // User Request to entirely delete the job
    case userRequest@Delete(sessionID, jobID) =>

      userJobs.getOrElseUpdate(sessionID, mutable.HashMap.empty[String, UserJob])
      if(!userWebSockets.contains(sessionID)) {
        Logger.info("Master does not know the SessionID " + sessionID)
        sender() ! SessionIDUnknown
      } else {

        if(!userJobs(sessionID).contains(jobID)) {

          Logger.info("JobID " + jobID + " is unknown for userID" + sessionID)
          sender() ! FrontendMasterProtocol.JobUnknown
        } else {

          Logger.info("JobID " + jobID + " will be deleted")
          val userJob = userJobs(sessionID).remove(jobID).get
          userJob.destroy()
          val work = Work(workIDCounter.toString, userRequest, userJob)
          workIDCounter += 1
          val event = WorkAccepted(work)
          workState = workState.updated(event)
          notifyWorkers()
        }
      }


    // User has requested his Job from the Master
    case userRequest@Get(sessionID, jobID) =>

      userJobs.getOrElseUpdate(sessionID, mutable.HashMap.empty[String, UserJob])
      if(!userWebSockets.contains(sessionID)) {

        Logger.info("Master does not know the SessionID " + sessionID)
        sender() ! SessionIDUnknown
      } else {

        if(!userJobs(sessionID).contains(jobID)) {

          Logger.info("JobID " + jobID + " is unknown for userID" + sessionID)
          sender() ! FrontendMasterProtocol.JobUnknown
        } else {

          val userJob = userJobs(sessionID)(jobID)
          sender() ! (userJob.getState, userJob.toolname, userJob.mainID)
        }
      }




    case MasterWorkerProtocol.WorkerRequestsWork(workerID) =>

      if (workState.hasWork) {

        workers.get(workerID) match {

          case Some(s @ WorkerState(_, Idle)) =>

            val work = workState.nextWork
            Logger.info("Master fetches work and gives it to Worker")

            // TODO PersistentActor would have to 'persist' 'event'
            val event = WorkStarted(work.workID)
            workState = workState.updated(event)
            log.info("Giving worker {} some work {}", workerID, work.workID)
            workers += (workerID -> s.copy(status = Busy(work.workID)))
            sender() ! work

          case _ =>
        }
      }

    case MasterWorkerProtocol.WorkIsDone(workerId, workId) =>

      // idempotent
      if (workState.isDone(workId)) {
        // previous Ack was lost, confirm again that this is done
        sender() ! MasterWorkerProtocol.Ack(workId)
      } else if (!workState.isInProgress(workId)) {
        log.info("Work {} not in progress, reported as done by worker {}", workId, workerId)
      } else {
        log.info("Work {} is done by worker {}", workId, workerId)
        changeWorkerToIdle(workerId, workId)

        // TODO PersistentActor would have to 'persist' 'event'
        val event = WorkCompleted(workId)
        workState = workState.updated(event)
        //mediator ! DistributedPubSubMediator.Publish(ResultsTopic, WorkResult(workId, result))
        // Ack back to original sender
        sender ! MasterWorkerProtocol.Ack(workId)

    }

    case MasterWorkerProtocol.WorkFailed(workerId, workId) =>

      if (workState.isInProgress(workId)) {
        log.info("Work {} failed by worker {}", workId, workerId)
        changeWorkerToIdle(workerId, workId)

        // TODO PersistentActor would have to 'persist' 'event'
        val event = WorkerFailed(workId)
        workState = workState.updated(event)
        notifyWorkers()
      }
  }
}



    /*

    // Asks the user actor to load a single job from the database in the JobModel
    case LoadJob (job_id : String) =>
      val dbJob_o = jobDB.get(user_id, job_id).headOption
      dbJob_o match {
        case Some(dbJob) => addJob(dbJob)
        case None =>
          ws match {
            case Some(webSocket) => webSocket ! JobIDInvalid
            case None =>
          }
      }

    // Sends the list of all jobs registered in the User Job
    case GetJobList =>
      ws match {
        case Some(webSocket) => webSocket ! SendJobList(userJobs.values.toSeq)
        case None =>
      }

    // Appends a new Job to a single parent job
    case AppendChildJob(parent_job_id, toolname, links) =>

      // Load the Parent job from the Database if it is not in the UserActor
      if(!userJobs.contains(parent_job_id)) {
        self ! LoadJob(parent_job_id)
      }

      // Generate new Job ID
      val job_id : String = checkOrGenerateJobID(None)
      val job = UserJob(self, toolname, job_id, user_id, Submitted,  false)

      // This is a new Job, so we have to make the Job state *Submitted* explicit
      job.changeState(Submitted)

      // Put the new job into the Database Mapping
      databaseMapping.put(job.job_id, jobRefDB.update(DBJob(None, job.job_id, user_id, job.getState, job.tool.toolname), session_id))
      userJobs.put(job.job_id, job)

      userJobs(parent_job_id).appendChild(job, links)


    case Convert(parent_job_id, child_job_id, links) =>
      worker ! WConvert(userJobs(parent_job_id), userJobs(child_job_id), links)


    // Connection To the WebSocket was ended
    case Terminated(ws_new) =>  ws.get ! PoisonPill

    // UserActor got to know that the job state has changed
    case UpdateJob(job : UserJob) =>

      // If the Job state is prepared and we want to start the job, then start
      if(job.getState == Prepared && job.startImmediate) {

        job.changeState(Queued)
        worker ! WStart(job)
      }

      if(userJobs.contains(job.job_id)) {

        // Forward Job state to Websocket
        ws match {
          case Some(webSocket) => webSocket ! UpdateJob(job)
          case None =>
        }

        // get the main ID
        val main_id_o = Some(databaseMapping.get(job.job_id).get.main_id)

        // update Job state in Persistence
        jobRefDB.update(DBJob(main_id_o, job.job_id, user_id, job.getState, job.tool.toolname), session_id)
      }

    // Sends a List of suggestions for the Auto Complete function
    case AutoComplete (suggestion : String) =>
      val dbJobSeq = jobDB.suggestJobID(user_id, suggestion)
      // Found something, return it to the user
      ws match {
        case Some(webSocket) => webSocket ! AutoCompleteSend(dbJobSeq)
        case None =>
      }





  }
    /*
    case MasterWorkerProtocol.RegisterWorker(workerId) =>
      if (workers.contains(workerId)) {
        workers += (workerId -> workers(workerId).copy(ref = sender()))
      } else {
        log.info("Worker registered: {}", workerId)
        workers += (workerId -> WorkerState(sender(), status = Idle))
        if (workState.hasWork)
          sender() ! MasterWorkerProtocol.WorkIsReady
      } */

    /*
    case MasterWorkerProtocol.WorkerRequestsWork(workerId) =>
      if (workState.hasWork) {
        workers.get(workerId) match {
          case Some(s @ WorkerState(_, Idle)) =>
            val work = workState.nextWork
            persist(WorkStarted(work.workId)) { event =>
              workState = workState.updated(event)
              log.info("Giving worker {} some work {}", workerId, work.workId)
              workers += (workerId -> s.copy(status = Busy(work.workId, Deadline.now + workTimeout)))
              sender() ! work
            }
          case _ =>
        }
      }*/

    /*


    case MasterWorkerProtocol.WorkFailed(workerId, workId) =>
      if (workState.isInProgress(workId)) {
        log.info("Work {} failed by worker {}", workId, workerId)
        changeWorkerToIdle(workerId, workId)
        persist(WorkerFailed(workId)) { event ⇒
          workState = workState.updated(event)
          notifyWorkers()
        }
      }

    case work: Work =>
      // idempotent
      if (workState.isAccepted(work.workId)) {
        sender() ! Master.Ack(work.workId)
      } else {
        log.info("Accepted work: {}", work.workId)
        persist(WorkAccepted(work)) { event ⇒
          // Ack back to original sender
          sender() ! Master.Ack(work.workId)
          workState = workState.updated(event)
          notifyWorkers()
        }
      }

    case CleanupTick =>
      for ((workerId, s @ WorkerState(_, Busy(workId, timeout))) ← workers) {
        if (timeout.isOverdue) {
          log.info("Work timed out: {}", workId)
          workers -= workerId
          persist(WorkerTimedOut(workId)) { event ⇒
            workState = workState.updated(event)
            notifyWorkers()
          }
        }
      }
  }

  def notifyWorkers(): Unit =
    if (workState.hasWork) {
      // could pick a few random instead of all
      workers.foreach {
        case (_, WorkerState(ref, Idle)) => ref ! MasterWorkerProtocol.WorkIsReady
        case _                           => // busy
      }
    }

  def changeWorkerToIdle(workerId: String, workId: String): Unit =
    workers.get(workerId) match {
      case Some(s @ WorkerState(_, Busy(`workId`, _))) ⇒
        workers += (workerId -> s.copy(status = Idle))
      case _ ⇒
      // ok, might happen after standby recovery, worker state is not persisted
    }
    */
  // TODO cleanup old workers
  // TODO cleanup old workIds, doneWorkIds

}
*/



