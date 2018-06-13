package actors

import java.time.ZonedDateTime

import javax.inject.Inject
import actors.ClusterMonitor.PolledJobs
import actors.JobActor._
import akka.actor._
import akka.event.LoggingReceive
import better.files._
import com.google.inject.assistedinject.Assisted
import de.proteinevolution.common.LocationProvider
import de.proteinevolution.db.MongoStore
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs.JobState._
import de.proteinevolution.models.database.jobs._
import de.proteinevolution.models.database.statistics.{ JobEvent, JobEventLog }
import de.proteinevolution.models.database.users.User
import de.proteinevolution.models.search.JobDAO
import de.proteinevolution.parsers.Ops.QStat
import de.proteinevolution.tel.TEL
import de.proteinevolution.tel.env.Env
import de.proteinevolution.tel.execution.ExecutionContext.FileAlreadyExists
import de.proteinevolution.tel.execution.WrapperExecutionFactory.RunningExecution
import de.proteinevolution.tel.execution.{ ExecutionContext, WrapperExecutionFactory }
import de.proteinevolution.tel.runscripts.Runscript.Evaluation
import de.proteinevolution.tel.runscripts._
import models.UserSessions
import models.mailing.MailTemplate.JobFinishedMail
import play.api.Configuration
import play.api.cache.{ NamedCache, SyncCacheApi }
import play.api.libs.mailer.MailerClient
import reactivemongo.bson.{ BSONDateTime, BSONDocument, BSONObjectID }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

object JobActor {

  case class PrepareJob(job: Job,
                        params: Map[String, String],
                        startJob: Boolean = false,
                        isInternalJob: Boolean = false)

  case class CheckJobHashes(jobID: String)

  // Messages the Job Actor to start a job
  case class StartJob(jobID: String)

  trait Factory {
    def apply(@Assisted("jobActorNumber") jobActorNumber: Int): Actor
  }

  // Messages the jobActor accepts from outside
  case class PushJob(job: Job)

  // Message for the Websocket Actor to send a ClearJob Message
  case class ClearJob(jobID: String, deleted: Boolean = false)

  // User Actor starts watching
  case class AddToWatchlist(jobID: String, userID: BSONObjectID)

  // checks if user has submitted max number of jobs
  // of jobs within a given time
  case class CheckIPHash(jobID: String)

  // UserActor Stops Watching this Job
  case class RemoveFromWatchlist(jobID: String, userID: BSONObjectID)

  // JobActor is requested to Delete the job
  case class Delete(jobID: String, userID: Option[BSONObjectID] = None)

  // Job Controller receives a job state change from the SGE or from any other valid source
  case class JobStateChanged(jobID: String, jobState: JobState)

  case class SetSGEID(jobID: String, sgeID: String)

  // show browser notification
  case class ShowNotification(notificationType: String, tag: String, title: String, body: String)

  // Job Controller receives push message to update the log
  case class UpdateLog(jobID: String)

  // forward filewatching task to ws actor

  case class WatchLogFile(job: Job)

  case object UpdateLog2

}

class JobActor @Inject()(
    runscriptManager: RunscriptManager, // To get runscripts to be executed
    env: Env, // To supply the runscripts with an environment
    implicit val mailerClient: MailerClient,
    val jobDao: JobDAO,
    mongoStore: MongoStore,
    userSessions: UserSessions,
    wrapperExecutionFactory: WrapperExecutionFactory,
    implicit val locationProvider: LocationProvider,
    @NamedCache("userCache") implicit val userCache: SyncCacheApi,
    @NamedCache("wsActorCache") implicit val wsActorCache: SyncCacheApi,
    constants: ConstantsV2,
    @Assisted("jobActorNumber") jobActorNumber: Int,
    config: Configuration
)(implicit ec: scala.concurrent.ExecutionContext)
    extends Actor
    with ActorLogging {

  // Attributes asssocidated with a Job
  @volatile private var currentJobs: Map[String, Job]                           = Map.empty[String, Job]
  @volatile private var currentJobLogs: Map[String, JobEventLog]                = Map.empty[String, JobEventLog]
  @volatile private var currentExecutionContexts: Map[String, ExecutionContext] = Map.empty[String, ExecutionContext]

  @volatile private var currentJobStrikes: Map[String, Int] = Map.empty[String, Int]

  // long polling stuff

  private val fetchLatestInterval = 1 seconds
  private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, UpdateLog2)(context.system.dispatcher)
  }

  // Running executions
  @volatile private var runningExecutions: Map[String, RunningExecution] = Map.empty

  /**
   * Get the job from the current jobs. If it is not there, get it from the DB.
   *
   * @param jobID
   * @return
   */
  private def getCurrentJob(jobID: String): Future[Option[Job]] = {
    // Check if the job is still in the current jobs.
    this.currentJobs.get(jobID) match {
      case Some(job) => // Everything is fine. Return the job.
        Future.successful(Some(job))
      case None => // Job is not in the current jobs.. try to get it back.
        mongoStore.findJob(BSONDocument(Job.JOBID -> jobID)).map {
          case Some(job) =>
            // Get the job back into the current jobs
            this.currentJobs = this.currentJobs.updated(job.jobID, job)
            // TODO Check if the job is a running job and also if the cluster has done any changes with on the job.
            // Return the job
            Some(job)
          case None =>
            // Something must have went wrong, the job is not in the DB.
            None
        }
    }
  }

  /**
   * Gets the execution context for a given jobID, even if it has been removed.
   *
   * @param jobID
   * @return
   */
  private def getCurrentExecutionContext(jobID: String): Option[ExecutionContext] = {
    this.currentExecutionContexts.get(jobID) match {
      case Some(executionContext) => Some(executionContext)
      case None =>
        if ((constants.jobPath / jobID).exists) {
          val executionContext = ExecutionContext(constants.jobPath / jobID, reOpen = true)
          this.currentExecutionContexts = this.currentExecutionContexts.updated(jobID, executionContext)
          Some(executionContext)
        } else {
          None
        }
    }
  }

  /**
   * Return the validated parameters
   *
   * @param job
   * @param runscript
   * @param params
   * @return
   */
  private def validatedParameters(job: Job,
                                  runscript: Runscript,
                                  params: Map[String, String]): Seq[(String, (Evaluation, Option[Argument]))] = {
    // Representation of the current State of the job submission

    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO
    // TODO Check parameters for validity here!!!             TODO
    // TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO TODO

    var validParameters: Seq[(String, (Evaluation, Option[Argument]))] =
      runscript.parameters.map(t => t._1 -> (t._2 -> Some(ValidArgument(new LiteralRepresentation(RString("false"))))))
    params.foreach { pv =>
      validParameters = supply(job.jobID, pv._1, pv._2, validParameters)
    }
    validParameters
  }

  /** Supplies a value for a particular Parameter. Returns params again if the parameter
   * is not present
   *
   * @param name
   * @param value
   * @param params
   */
  private def supply(
      jobID: String,
      name: String,
      value: String,
      params: Seq[(String, (Runscript.Evaluation, Option[Argument]))]
  ): Seq[(String, (Runscript.Evaluation, Option[Argument]))] = {
    params.map {
      case (paramName, (evaluation, _)) if paramName == name =>
        val x = Some(evaluation(RString(value), this.currentExecutionContexts(jobID)))
        (name, (evaluation, x))
      case q => q
    }
  }

  /**
   * JobActor removes the Job with the matching jobID from its maps
   * @param jobID
   */
  private def removeJob(jobID: String): Boolean = {
    var wasActive = this.currentJobs.contains(jobID)
    // If the job is in the current jobs remove it
    if (wasActive) {
      this.currentJobs = this.currentJobs.filter(_._1 != jobID)
    }

    // Save Job Event Log to the collection and remove it from the map afterwards
    if (this.currentJobLogs.contains(jobID)) {
      mongoStore.addJobLog(this.currentJobLogs(jobID))
      this.currentJobLogs = this.currentJobLogs.filter(_._1 != jobID)
      wasActive = true
    }

    // If the job appears in the running Execution, terminate it
    if (this.runningExecutions.contains(jobID)) {
      this.runningExecutions(jobID).terminate()
      this.runningExecutions = this.runningExecutions.filter(_._1 != jobID)
      wasActive = true
    }

    // if the job appears in the current execution contexts, remove it from there too
    if (this.currentExecutionContexts.contains(jobID)) {
      this.currentExecutionContexts = this.currentExecutionContexts.filter(_._1 != jobID)
      wasActive = true
    }
    wasActive
  }

  /**
   * Deletes a job from all instances and tells all the watching users about it
   * @param job the job to be deleted
   */
  private def delete(job: Job, verbose: Boolean): Unit = {
    val now: ZonedDateTime = ZonedDateTime.now
    if (verbose) log.info(s"[JobActor.Delete] Deletion of job folder for jobID ${job.jobID} is done")
    s"${constants.jobPath}${job.jobID}".toFile.delete(true)
    if (verbose) log.info("[JobActor.Delete] Removing Job from current Jobs.")
    this.removeJob(job.jobID) // Remove the job from the current job map
    // Message user clients to remove the job from their watchlist
    if (verbose) log.info(s"[JobActor.Delete] Informing Users of deletion of Job with JobID ${job.jobID}.")
    val foundWatchers = job.watchList.flatMap(userID => wsActorCache.get(userID.stringify): Option[List[ActorRef]])
    foundWatchers.flatten.foreach(_ ! ClearJob(job.jobID))

    mongoStore.eventLogCollection
      .flatMap(
        _.findAndUpdate(
          BSONDocument(JobEventLog.IDDB -> job.mainID),
          BSONDocument(
            "$push" ->
            BSONDocument(JobEventLog.EVENTS -> JobEvent(Deleted, Some(now), 0))
          ),
          fetchNewObject = true
        ).map(_.result[JobEventLog])
      )
      .foreach { jobEventLogOpt =>
        if (verbose) log.info(s"""[JobActor.Delete] Event Log: ${jobEventLogOpt match {
                                   case Some(x) => x.toString
                                   case None    => ""
                                 }}""".stripMargin)
      }

    // Remove the job from mongoDB collection
    mongoStore.removeJob(BSONDocument(Job.IDDB -> job.mainID)).foreach { writeResult =>
      if (writeResult.ok) {
        if (verbose) log.info(s"[JobActor.Delete] Deletion of Job was successful:\n${job.toString()}")
      } else {
        if (verbose)
          log.info(s"[JobActor.Delete] Deleting the job with jobID ${job.jobID} from the collection failed.")
      }
    }
    if (verbose) log.info(s"[JobActor.Delete] Deletion of job with jobID ${job.jobID} Complete.")
  }

  /**
   * Updates Jobstate in Model, in database, and notifies user watchlist
   */
  private def updateJobState(job: Job): Future[Job] = {
    // Push the updated job into the current jobs
    this.currentJobs = this.currentJobs.updated(job.jobID, job)

    // Update job in the database and notify watcher upon completion
    mongoStore
      .modifyJob(BSONDocument(Job.IDDB -> job.mainID), BSONDocument("$set" -> BSONDocument(Job.STATUS -> job.status)))
      .map { modifiedJob =>
        val jobLog = this.currentJobLogs.get(job.jobID) match {
          case Some(jobEventLog) => jobEventLog.addJobStateEvent(job.status)
          case None =>
            JobEventLog(mainID = job.mainID,
                        toolName = job.tool,
                        events = List(JobEvent(job.status, Some(ZonedDateTime.now))))
        }
        this.currentJobLogs = this.currentJobLogs.updated(job.jobID, jobLog)
        val foundWatchers = job.watchList.flatMap(userID => wsActorCache.get(userID.stringify): Option[List[ActorRef]])
        foundWatchers.flatten.foreach(_ ! PushJob(job))
        if (job.status == Done) {
          foundWatchers.flatten.foreach(
            _ ! ShowNotification(
              "job_update",
              job.jobID,
              "Job Update",
              "Your " + config.get[String](s"Tools.${job.tool}.longname") + " job has finished!"
            )
          )
        }
        job
      }

  }

  /**
   * Determines whether the parameter list is completely supplied
   *
   * @param params
   * @return
   */
  private def isComplete(params: Seq[(String, (Runscript.Evaluation, Option[Argument]))]): Boolean = {
    // If we have an argument for all parameters, we are done
    params.forall(item => item._2._2.isDefined)
  }

  /**
   * Sends an eMail to the owner of the job
   *
   * @param job
   * @return
   */
  private def sendJobUpdateMail(job: Job): Boolean = {
    if (job.emailUpdate && job.ownerID.isDefined) {
      mongoStore.findUser(BSONDocument(User.IDDB -> job.ownerID)).foreach {
        case Some(user) =>
          user.userData match {
            case Some(_) =>
              log.info(
                s"[JobActor[$jobActorNumber].sendJobUpdateMail] Sending eMail to job owner for job ${job.jobID}: Job is ${job.status.toString}"
              )
              val eMail = JobFinishedMail(user, job)
              eMail.send
            case None =>
          }
        case None =>
      }
      true
    } else {
      false
    }
  }

  override def postStop(): Unit = {
    val _ = Tick.cancel()
  }
  override def receive = LoggingReceive {

    case PrepareJob(job, params, startJob, isInternalJob) =>
      // jobid will also be available as parameter
      val extendedParams = params + ("jobid" -> job.jobID)

      // Add job to the current jobs
      this.currentJobs = this.currentJobs.updated(job.jobID, job)

      try {
        // Establish execution context for the new Job
        val executionContext = ExecutionContext(constants.jobPath / job.jobID)
        this.currentExecutionContexts = this.currentExecutionContexts.updated(job.jobID, executionContext)

        // Create a log for this job
        this.currentJobLogs =
          this.currentJobLogs.updated(job.jobID,
                                      JobEventLog(mainID = job.mainID,
                                                  toolName = job.tool,
                                                  internalJob = isInternalJob,
                                                  events = List(JobEvent(job.status, Some(ZonedDateTime.now)))))

        // Get new runscript instance from the runscript manager
        val runscript: Runscript = runscriptManager(job.tool).withEnvironment(env)

        // Validate the Parameters right away
        val validParameters = this.validatedParameters(job, runscript, extendedParams)

        // adds the params of the disabled controls from formData, sets value of those to "false"
        validParameters.filterNot(pv => extendedParams.contains(pv._1)).foreach { pv =>
          extendedParams.+(pv._1 -> "false")
        }

        // Serialize the JobParameters to the JobDirectory
        // Store the extended Parameters in the working directory for faster reloading
        executionContext.writeParams(extendedParams)

        if (isComplete(validParameters)) {
          // When the user wants to force the job to start without job hash check, then this will jump right to prepared
          if (startJob) {
            self ! CheckIPHash(job.jobID)
          } else {
            log.info("JobID " + job.jobID + " will now be hashed.")
            self ! CheckJobHashes(job.jobID)
          }
        } else {
          // TODO Implement Me. This specifies what the JobActor should do
          // TODO when not all parameters have been specified or when they are invalid
          log.error("[JobActor.PrepareJob] The job " + job.jobID + " has invalid or missing parameters.")
          self ! JobStateChanged(job.jobID, Error)
        }
      } catch {
        case FileAlreadyExists(_) =>
          log.error(
            "[JobActor.PrepareJob] The directory for job " + job.jobID + " already exists\n" +
            "[JobActor.PrepareJob] Stopping job since it can not be retrieved by user."
          )
          self ! JobStateChanged(job.jobID, Error)
      }

    // Checks the jobHashDB for matches and generates one for the job if there are none.
    case CheckJobHashes(jobID) =>
      log.info(s"[JobActor[$jobActorNumber].CheckJobHashes] Job with jobID $jobID will now be hashed.")
      this.getCurrentJob(jobID).foreach {
        case Some(job) =>
          this.getCurrentExecutionContext(jobID) match {
            case Some(executionContext) =>
              // Ensure that the jobID is not being hashed
              val params  = executionContext.reloadParams
              val jobHash = jobDao.generateJobHash(job, params, env)
              log.info(s"[JobActor[$jobActorNumber].CheckJobHashes] Job hash: " + jobHash)
              // Find the Jobs in the Database
              mongoStore
                .findAndSortJobs(
                  BSONDocument(Job.HASH        -> jobHash),
                  BSONDocument(Job.DATECREATED -> -1)
                )
                .foreach { jobList =>
                  // Check if the jobs are owned by the user, unless they are public and if the job is Done
                  jobList.find(
                    filterJob => (filterJob.isPublic || filterJob.ownerID == job.ownerID) && filterJob.status == Done
                  ) match {
                    case Some(oldJob) =>
                      log.info(
                        s"[JobActor[$jobActorNumber].CheckJobHashes] JobID $jobID is a duplicate of ${oldJob.jobID}."
                      )
                      self ! JobStateChanged(job.jobID, Pending)
                    case None =>
                      log.info(s"[JobActor[$jobActorNumber].CheckJobHashes] JobID $jobID will now be started.")
                      self ! CheckIPHash(job.jobID)
                  }
                }
            case None =>
              log.error(
                s"[JobActor[$jobActorNumber].CheckJobHashes] Could not recreate execution context for jobID " + jobID
              )
          }
        case None =>
          log.error(
            s"[JobActor[$jobActorNumber].CheckJobHashes] Could not find the jobID " + jobID + " in the Cache or DB."
          )
      }

    case Delete(jobID, userIDOption) =>
      val verbose = true // just switch this on / off for logging
      if (verbose) log.info(s"[JobActor[$jobActorNumber].Delete] Received Delete for $jobID")
      this
        .getCurrentJob(jobID)
        .flatMap {
          case Some(job) => Future.successful(Some(job))
          case None =>
            if (verbose)
              log.info(
                s"[JobActor[$jobActorNumber].Delete] jobID $jobID not found in current jobs. Loading job from DB."
              )
            mongoStore.findJob(BSONDocument(Job.JOBID -> jobID))
        }
        .foreach {
          case Some(job) =>
            // Delete the job when the user is the owner and clear it otherwise
            if (userIDOption.isEmpty || userIDOption == job.ownerID) {
              if (verbose)
                log.info(s"[JobActor[$jobActorNumber].Delete] Found Job with ${job.jobID} - starting file deletion")
              this.delete(job, verbose)
            } else {
              userIDOption match {
                case Some(userID) =>
                  self ! RemoveFromWatchlist(jobID, userID)
                case None =>
              }
            }
          case None =>
            log.error(s"[JobActor[$jobActorNumber].Delete] Could not find job with JobID $jobID.")
        }

    case CheckIPHash(jobID) =>
      this.getCurrentJob(jobID).foreach {
        case Some(job) =>
          job.IPHash match {
            case Some(hash) =>
              val selector = BSONDocument(
                "$and" ->
                List(
                  BSONDocument(Job.IPHASH -> hash),
                  BSONDocument(
                    Job.DATECREATED ->
                    BSONDocument(
                      "$gt" -> BSONDateTime(
                        ZonedDateTime.now.minusMinutes(constants.maxJobsWithin.toLong).toInstant.toEpochMilli
                      )
                    )
                  )
                )
              )

              val selectorDay = BSONDocument(
                "$and" ->
                List(
                  BSONDocument(Job.IPHASH -> hash),
                  BSONDocument(
                    Job.DATECREATED ->
                    BSONDocument(
                      "$gt" -> BSONDateTime(
                        ZonedDateTime.now.minusDays(constants.maxJobsWithinDay.toLong).toInstant.toEpochMilli
                      )
                    )
                  )
                )
              )
              mongoStore.countJobs(selector).map { count =>
                mongoStore.countJobs(selectorDay).map { countDay =>
                  log
                    .info(
                      BSONDateTime(
                        ZonedDateTime.now.minusMinutes(constants.maxJobsWithin.toLong).toInstant.toEpochMilli
                      ).toString
                    )
                  log.info(
                    s"[JobActor[$jobActorNumber].StartJob] IP ${job.IPHash} has requested $count jobs within the last ${constants.maxJobsWithin} minute and $countDay within the last 24 hours."
                  )
                  if (count <= constants.maxJobNum && countDay <= constants.maxJobNumDay) {
                    self ! StartJob(job.jobID)
                  } else {
                    self ! JobStateChanged(job.jobID, LimitReached)
                  }
                }
              }
            case None =>
              // TODO: remove this as soon as possible, because soon all jobs should hold the hashed IP
              self ! StartJob(job.jobID)
          }
        case None =>
      }

    case StartJob(jobID) =>
      this.getCurrentJob(jobID).foreach {
        case Some(job) =>
          this.getCurrentExecutionContext(jobID) match {
            case Some(executionContext) =>
              log.info(s"[JobActor[$jobActorNumber].StartJob] reached. starting job " + jobID)

              // get the params
              val params = executionContext.reloadParams
              // generate job hash
              val jobHash = Some(jobDao.generateJobHash(job, params, env))

              // Set memory allocation on the cluster and let the clusterMonitor define the multiplier.
              // To receive a catchable signal in an SGE job, one must set soft limits
              // in addition to hard limits; by definition "hard" means SIGKILL.

              val h_rt = config.get[Int](s"Tools.${job.tool}.hardruntime")

              //Set soft runtime to 30s less than hard runtime
              val s_rt   = h_rt - 30
              val h_vmem = (config.get[Int](s"Tools.${job.tool}.memory") * TEL.memFactor).toInt
              //Set soft memory limit to 95% of hard memory limit
              val s_vmem  = h_vmem * 0.95
              val threads = math.ceil(config.get[Int](s"Tools.${job.tool}.threads") * TEL.threadsFactor).toInt

              env.configure(s"MEMORY", h_vmem.toString + "G")
              env.configure(s"SOFTMEMORY", s_vmem.toString + "G")
              env.configure(s"THREADS", threads.toString)
              env.configure(s"HARDRUNTIME", h_rt.toString)
              env.configure(s"SOFTRUNTIME", s_rt.toString)

              log.info(s"$jobID is running with $h_vmem GB h_vmem")
              log.info(s"$jobID is running with $threads threads")
              log.info(s"$jobID is running with $h_rt h_rt")

              val clusterData = JobClusterData("", Some(h_vmem), Some(threads), Some(h_rt))

              mongoStore
                .modifyJob(
                  BSONDocument(Job.IDDB -> job.mainID),
                  BSONDocument("$set"   -> BSONDocument(Job.CLUSTERDATA -> clusterData, Job.HASH -> jobHash))
                )
                .foreach {
                  case Some(_) =>
                    // Get new runscript instance from the runscript manager
                    val runscript: Runscript = runscriptManager(job.tool).withEnvironment(env)
                    // Load the parameters from the serialized parameters file
                    val params = executionContext.reloadParams
                    // Validate the Parameters (again) to ensure that everything works
                    val validParameters = this.validatedParameters(job, runscript, params)

                    // adds the params of the disabled controls from formData, sets value of those to "false"
                    validParameters.filterNot(pv => params.contains(pv._1)).foreach { pv =>
                      params.+(pv._1 -> "false")
                    }

                    if (isComplete(validParameters)) {
                      val pendingExecution = wrapperExecutionFactory.getInstance(
                        runscript(validParameters.map(t => (t._1, t._2._2.get.asInstanceOf[ValidArgument])))
                      )

                      if (!executionContext.blocked) {

                        executionContext.accept(pendingExecution)
                        log.info(s"[JobActor[$jobActorNumber].StartJob] Running job now.")
                        this.runningExecutions =
                          this.runningExecutions.updated(job.jobID, executionContext.executeNext.run())
                      }
                    } else {
                      // TODO Implement Me. This specifies what the JobActor should do if not all parameters have been specified
                      log.info("STAY")
                    }

                    self ! JobStateChanged(job.jobID, Prepared)
                  case None =>
                    log.error(s"[JobActor[$jobActorNumber].StartJob] Job could not be written to DB: " + jobID)
                }
            //env.remove(s"MEMORY")
            //env.remove(s"THREADS")
            case None =>
          }
        case None =>
          log.error(s"[JobActor[$jobActorNumber].StartJob] Job not found in DB: " + jobID)
      }

    // User Starts watching job
    case AddToWatchlist(jobID, userID) =>
      val _ = mongoStore
        .modifyJob(BSONDocument(Job.JOBID -> jobID), BSONDocument("$addToSet" -> BSONDocument(Job.WATCHLIST -> userID)))
        .map {
          case Some(updatedJob) =>
            userSessions
              .modifyUserWithCache(BSONDocument(User.IDDB   -> userID),
                                   BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> jobID)))
              .foreach { _ =>
                this.currentJobs = this.currentJobs.updated(jobID, updatedJob)
                val wsActors = wsActorCache.get(userID.stringify): Option[List[ActorRef]]
                wsActors.foreach(_.foreach(_ ! PushJob(updatedJob)))
              }
          case None => ()
        }

    // User does no longer watch this Job (stays in JobManager)
    case RemoveFromWatchlist(jobID, userID) =>
      mongoStore
        .modifyJob(BSONDocument(Job.JOBID -> jobID), BSONDocument("$pull" -> BSONDocument(Job.WATCHLIST -> userID)))
        .foreach {
          case Some(updatedJob) =>
            userSessions
              .modifyUserWithCache(BSONDocument(User.IDDB -> userID),
                                   BSONDocument("$pull"   -> BSONDocument(User.JOBS -> jobID)))
              .foreach { _ =>
                this.currentJobs = this.currentJobs.updated(jobID, updatedJob)
                val wsActors = wsActorCache.get(userID.stringify): Option[List[ActorRef]]
                wsActors.foreach(_.foreach(_ ! ClearJob(jobID)))
              }
          case None => ()
        }

    // Message from outside that the jobState has changed
    case JobStateChanged(jobID: String, jobState: JobState) =>
      this.getCurrentJob(jobID).foreach {
        case Some(oldJob) =>
          // Update the job object
          val job = oldJob.copy(status = jobState)
          // Give a update message to all
          log.info(
            s"[JobActor[$jobActorNumber].JobStateChanged] State has changed to ${job.status.toString} for the Job with the JobID ${job.jobID}"
          )

          // Dependent on the state, we have to do different things
          if (job.isFinished) {
            // Now we can update the JobState and remove it, once the update has completed
            this.updateJobState(job).map { job =>
              //Remove the job from the jobActor
              this.removeJob(job.jobID)
              // Tell the user that their job finished via eMail (can be either failed or done)
              sendJobUpdateMail(job)
            }
          } else {
            this.updateJobState(job)
          }
        case None =>
          log.info(s"[JobActor[$jobActorNumber].JobStateChanged] Job not found: " + jobID)
      }

    // Checks the current jobs against the currently running cluster jobs to see if there are any dead jobs
    case PolledJobs(qStat: QStat) =>
      val clusterJobIDs = qStat.qStatJobs.map(_.sgeID)
      //if(this.currentJobs.nonEmpty)
      //log.info(s"[JobActor[$jobActorNumber].PolledJobs] sge Jobs to check: ${clusterJobIDs.mkString(", ")}\nactor Jobs to check:${this.currentJobs.values.flatMap(_.clusterData.map(_.sgeID)).mkString(", ")}")
      this.currentJobs.values.foreach { job =>
        job.clusterData match {
          case Some(clusterData) =>
            val jobInCluster = clusterJobIDs.contains(clusterData.sgeID)
            //log.info(s"[JobActor[$jobActorNumber].PolledJobs] Job ${job.jobID} with sgeID ${clusterData.sgeID}: ${if(jobInCluster) "active" else "inactive"}")
            if (!job.isFinished && !jobInCluster) {
              val strikes = this.currentJobStrikes.getOrElse(job.jobID, 0) + 1
              if (strikes >= constants.pollingMaximumStrikes) {
                this.currentJobStrikes = this.currentJobStrikes.filter(_._1 != job.jobID)
                self ! JobStateChanged(job.jobID, Error)
              } else {
                this.currentJobStrikes = this.currentJobStrikes.updated(job.jobID, strikes)
                log.info(s"[JobActor[$jobActorNumber].PolledJobs] Job ${job.jobID} strikes: $strikes.")
              }
            }
          case None =>
          //log.info(s"[JobActor[$jobActorNumber].PolledJobs] Job ${job.jobID} has no SGE data yet.")
        }
      }

    // Sets the cluster job ID for a job
    case SetSGEID(jobID: String, sgeID: String) =>
      mongoStore
        .modifyJob(BSONDocument(Job.JOBID -> jobID), BSONDocument("$set" -> BSONDocument(Job.SGEID -> sgeID)))
        .foreach {
          case Some(job) =>
            this.currentJobs = this.currentJobs.updated(job.jobID, job)
          case None =>
        }

    // gets updatelog notifications via curl
    case UpdateLog(jobID: String) =>
      currentJobs.get(jobID) match {
        case Some(job) =>
          val foundWatchers =
            job.watchList.flatMap(userID => wsActorCache.get(userID.stringify): Option[List[ActorRef]])
          job.status match {
            case Running => foundWatchers.flatten.foreach(_ ! WatchLogFile(job))
            case _ =>
              foundWatchers.flatten.foreach(_ ! WatchLogFile(job))
              foundWatchers.flatten.foreach(_ ! PushJob(job))
          }

        case None =>
      }

    // does longpolling

    case UpdateLog2 =>
      currentJobs.foreach { job =>
        val foundWatchers =
          job._2.watchList.flatMap(userID => wsActorCache.get(userID.stringify): Option[List[ActorRef]])
        job._2.status match {
          case Running => foundWatchers.flatten.foreach(_ ! WatchLogFile(job._2))
          case _       =>
        }

      }
  }
}
