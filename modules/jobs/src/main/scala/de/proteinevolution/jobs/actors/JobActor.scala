package de.proteinevolution.jobs.actors

import java.time.ZonedDateTime

import akka.NotUsed
import akka.actor._
import akka.event.LoggingReceive
import better.files._
import com.google.inject.assistedinject.Assisted
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.models.MailTemplate.JobFinishedMail
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.base.helpers.ToolkitTypes
import de.proteinevolution.cluster.api.Polling.PolledJobs
import de.proteinevolution.cluster.api.{ QStat, Qdel }
import de.proteinevolution.jobs.actors.JobActor._
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.{ Job, JobClusterData }
import de.proteinevolution.jobs.services.{ GeneralHashService, JobTerminator }
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs.JobState._
import de.proteinevolution.models.database.jobs._
import de.proteinevolution.models.database.statistics.{ JobEvent, JobEventLog }
import de.proteinevolution.models.database.users.User
import de.proteinevolution.tel.TEL
import de.proteinevolution.tel.env.Env
import de.proteinevolution.tel.execution.ExecutionContext.FileAlreadyExists
import de.proteinevolution.tel.execution.WrapperExecutionFactory.RunningExecution
import de.proteinevolution.tel.execution.{ ExecutionContext, WrapperExecutionFactory }
import de.proteinevolution.tel.runscripts.Runscript.Evaluation
import de.proteinevolution.tel.runscripts._
import javax.inject.Inject
import play.api.cache.{ NamedCache, SyncCacheApi }
import play.api.libs.mailer.MailerClient
import play.api.{ Configuration, Environment }
import reactivemongo.bson.{ BSONDateTime, BSONDocument, BSONObjectID }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class JobActor @Inject()(
    runscriptManager: RunscriptManager,
    environment: Environment,
    env: Env,
    hashService: GeneralHashService,
    jobDao: JobDao,
    userDao: UserDao,
    userSessions: UserSessionService,
    wrapperExecutionFactory: WrapperExecutionFactory,
    @NamedCache("wsActorCache") wsActorCache: SyncCacheApi,
    constants: ConstantsV2,
    @Assisted("jobActorNumber") jobActorNumber: Int,
    config: Configuration
)(implicit ec: scala.concurrent.ExecutionContext, mailerClient: MailerClient)
    extends Actor
    with ActorLogging
    with ToolkitTypes
    with JobTerminator {

  // Attributes asssocidated with a Job
  @volatile private var currentJobs: Map[String, Job]                           = Map.empty[String, Job]
  @volatile private var currentJobLogs: Map[String, JobEventLog]                = Map.empty[String, JobEventLog]
  @volatile private var currentExecutionContexts: Map[String, ExecutionContext] = Map.empty[String, ExecutionContext]

  private val fetchLatestInterval = 500 millis
  private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, UpdateLog)(context.system.dispatcher)
  }

  // Running executions
  @volatile private var runningExecutions: Map[String, RunningExecution] = Map.empty

  private def getCurrentJob(jobID: String): Future[Option[Job]] = {
    // Check if the job is still in the current jobs.
    currentJobs.get(jobID) match {
      case Some(job) => // Everything is fine. Return the job.
        fuccess(Some(job))
      case None => // Job is not in the current jobs.. try to get it back.
        jobDao.findJob(BSONDocument(Job.JOBID -> jobID)).map {
          case Some(job) =>
            // Get the job back into the current jobs
            currentJobs = currentJobs.updated(job.jobID, job)
            // TODO Check if the job is a running job and also if the cluster has done any changes with on the job.
            // Return the job
            Some(job)
          case None =>
            // Something must have went wrong, the job is not in the DB.
            None
        }
    }
  }

  private def getCurrentExecutionContext(jobID: String): Option[ExecutionContext] = {
    currentExecutionContexts.get(jobID) match {
      case Some(executionContext) => Some(executionContext)
      case None =>
        if ((constants.jobPath / jobID).exists) {
          val executionContext = ExecutionContext(constants.jobPath / jobID, reOpen = true)
          currentExecutionContexts = currentExecutionContexts.updated(jobID, executionContext)
          Some(executionContext)
        } else {
          None
        }
    }
  }

  private def validatedParameters(
      job: Job,
      runscript: Runscript,
      params: Map[String, String]
  ): Seq[(String, (Evaluation, Option[Argument]))] = {
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

  private def supply(
      jobID: String,
      name: String,
      value: String,
      params: Seq[(String, (Runscript.Evaluation, Option[Argument]))]
  ): Seq[(String, (Runscript.Evaluation, Option[Argument]))] = {
    params.map {
      case (paramName, (evaluation, _)) if paramName == name =>
        val x = Some(evaluation(RString(value), currentExecutionContexts(jobID)))
        (name, (evaluation, x))
      case q => q
    }
  }

  private def removeJob(jobID: String): Boolean = {
    var wasActive = currentJobs.contains(jobID)
    // If the job is in the current jobs remove it
    if (wasActive) {
      currentJobs = currentJobs.filter(_._1 != jobID)
    }

    // Save Job Event Log to the collection and remove it from the map afterwards
    if (currentJobLogs.contains(jobID)) {
      jobDao.addJobLog(currentJobLogs(jobID))
      currentJobLogs = currentJobLogs.filter(_._1 != jobID)
      wasActive = true
    }

    // If the job appears in the running Execution, terminate it
    if (runningExecutions.contains(jobID)) {
      runningExecutions(jobID).terminate()
      runningExecutions = runningExecutions.filter(_._1 != jobID)
      wasActive = true
    }

    // if the job appears in the current execution contexts, remove it from there too
    if (currentExecutionContexts.contains(jobID)) {
      currentExecutionContexts = currentExecutionContexts.filter(_._1 != jobID)
      wasActive = true
    }
    wasActive
  }

  private def delete(job: Job, verbose: Boolean): Unit = {
    val now: ZonedDateTime = ZonedDateTime.now
    if (verbose) log.info(s"[JobActor.Delete] Deletion of job folder for jobID ${job.jobID} is done")
    s"${constants.jobPath}${job.jobID}".toFile.delete(true)
    if (verbose) log.info("[JobActor.Delete] Removing Job from current Jobs.")
    removeJob(job.jobID) // Remove the job from the current job map
    // Message user clients to remove the job from their watchlist
    if (verbose) log.info(s"[JobActor.Delete] Informing Users of deletion of Job with JobID ${job.jobID}.")
    val foundWatchers = job.watchList.flatMap(userID => wsActorCache.get(userID.stringify): Option[List[ActorRef]])
    foundWatchers.flatten.foreach(_ ! ClearJob(job.jobID))
    // execute Qdel in case the job is still queued or running
    job.clusterData.foreach(clusterData => Qdel.run(clusterData.sgeID))
    jobDao.eventLogCollection
      .flatMap(
        _.findAndUpdate(
          BSONDocument(JobEventLog.JOBID -> job.jobID),
          BSONDocument(
            "$push" ->
            BSONDocument(JobEventLog.EVENTS -> JobEvent(Deleted, Some(now), Some(0L)))
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
    jobDao.removeJob(BSONDocument(Job.JOBID -> job.jobID)).foreach { writeResult =>
      if (writeResult.ok) {
        if (verbose) log.info(s"[JobActor.Delete] Deletion of Job was successful:\n${job.toString()}")
      } else {
        if (verbose)
          log.info(s"[JobActor.Delete] Deleting the job with jobID ${job.jobID} from the collection failed.")
      }
    }
    if (verbose) log.info(s"[JobActor.Delete] Deletion of job with jobID ${job.jobID} Complete.")
  }

  private def updateJobState(job: Job): Future[Job] = {
    // Push the updated job into the current jobs
    currentJobs = currentJobs.updated(job.jobID, job)

    // Update job in the database and notify watcher upon completion
    jobDao
      .modifyJob(BSONDocument(Job.JOBID -> job.jobID), BSONDocument("$set" -> BSONDocument(Job.STATUS -> job.status)))
      .map { _ =>
        val jobLog = currentJobLogs.get(job.jobID) match {
          case Some(jobEventLog) => jobEventLog.addJobStateEvent(job.status)
          case None =>
            JobEventLog(jobID = job.jobID,
                        toolName = job.tool,
                        events = List(JobEvent(job.status, Some(ZonedDateTime.now))))
        }
        currentJobLogs = currentJobLogs.updated(job.jobID, jobLog)
        val foundWatchers = job.watchList.flatMap(userID => wsActorCache.get(userID.stringify): Option[List[ActorRef]])
        foundWatchers.flatten.foreach(_ ! PushJob(job))
        if (job.status == Done) {
          foundWatchers.flatten.foreach(
            _ ! ShowJobNotification(
              job.jobID,
              "jobs.notifications.titles.update",
              "jobs.notifications.jobFinished"
            )
          )
        }
        job
      }

  }

  private def isComplete(params: Seq[(String, (Runscript.Evaluation, Option[Argument]))]): Boolean = {
    // If we have an argument for all parameters, we are done
    params.forall(item => item._2._2.isDefined)
  }

  private def sendJobUpdateMail(job: Job): Boolean = {
    if (job.emailUpdate && job.ownerID.isDefined) {
      userDao.findUser(BSONDocument(User.IDDB -> job.ownerID)).foreach {
        case Some(user) =>
          user.userData match {
            case Some(_) =>
              log.info(
                s"[JobActor[$jobActorNumber].sendJobUpdateMail] Sending eMail to job owner for job ${job.jobID}: Job is ${job.status.toString}"
              )
              val eMail = JobFinishedMail(user, job.jobID, job.status, environment, env)
              eMail.send
            case None => NotUsed
          }
        case None => NotUsed
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
      currentJobs = currentJobs.updated(job.jobID, job)

      try {
        // Establish execution context for the new Job
        val executionContext = ExecutionContext(constants.jobPath / job.jobID)
        currentExecutionContexts = currentExecutionContexts.updated(job.jobID, executionContext)

        // Create a log for this job
        currentJobLogs =
          currentJobLogs.updated(job.jobID,
                                 JobEventLog(jobID = job.jobID,
                                             toolName = job.tool,
                                             internalJob = isInternalJob,
                                             events = List(JobEvent(job.status, Some(ZonedDateTime.now)))))

        // Get new runscript instance from the runscript manager
        val runscript: Runscript = runscriptManager(job.tool).withEnvironment(env)

        // Validate the Parameters right away
        val validParameters = validatedParameters(job, runscript, extendedParams)

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
      getCurrentJob(jobID).foreach {
        case Some(job) =>
          getCurrentExecutionContext(jobID) match {
            case Some(executionContext) =>
              // Ensure that the jobID is not being hashed
              val params  = executionContext.reloadParams
              val jobHash = hashService.generateJobHash(job, params, env)
              log.info(s"[JobActor[$jobActorNumber].CheckJobHashes] Job hash: " + jobHash)
              // Find the Jobs in the Database
              jobDao
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
            case None => NotUsed
          }
        case None => NotUsed
      }

    case Delete(jobID, userIDOption) =>
      val verbose = true // just switch this on / off for logging
      if (verbose) log.info(s"[JobActor[$jobActorNumber].Delete] Received Delete for $jobID")
      this
        .getCurrentJob(jobID)
        .flatMap {
          case Some(job) => fuccess(Some(job))
          case None =>
            if (verbose)
              log.info(
                s"[JobActor[$jobActorNumber].Delete] jobID $jobID not found in current jobs. Loading job from DB."
              )
            jobDao.findJob(BSONDocument(Job.JOBID -> jobID))
        }
        .foreach {
          case Some(job) =>
            // Delete the job when the user is the owner and clear it otherwise
            if (userIDOption.isEmpty || userIDOption == job.ownerID) {
              if (verbose)
                log.info(s"[JobActor[$jobActorNumber].Delete] Found Job with ${job.jobID} - starting file deletion")
              delete(job, verbose)
            } else {
              userIDOption match {
                case Some(userID) =>
                  self ! RemoveFromWatchlist(jobID, userID)
                case None => NotUsed
              }
            }
          case None => NotUsed
        }

    case CheckIPHash(jobID) =>
      getCurrentJob(jobID).foreach {
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
              jobDao.countJobs(selector).map { count =>
                jobDao.countJobs(selectorDay).map { countDay =>
                  log.info(
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
        case None => NotUsed
      }

    case StartJob(jobID) =>
      getCurrentJob(jobID).foreach {
        case Some(job) =>
          getCurrentExecutionContext(jobID) match {
            case Some(executionContext) =>
              log.info(s"[JobActor[$jobActorNumber].StartJob] reached. starting job " + jobID)

              // get the params
              val params = executionContext.reloadParams
              // generate job hash
              val jobHash = Some(hashService.generateJobHash(job, params, env))

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
              env.configure(s"SUBMITMODE", config.get[String]("submit_mode"))
              env.configure(s"SGENODES", config.get[String]("sge_nodes"))
              env.configure(s"DATABASES", config.get[String]("db_root"))
              env.configure(s"BIOPROGSROOT", config.get[String]("bioprogs_root"))
              env.configure(s"HOSTNAME", config.get[String]("host_name"))
              env.configure(s"PORT", config.get[String]("port"))

              log.info(s"$jobID is running with $h_vmem GB h_vmem")
              log.info(s"$jobID is running with $threads threads")
              log.info(s"$jobID is running with $h_rt h_rt")

              val clusterData = JobClusterData("", Some(h_vmem), Some(threads), Some(h_rt))

              jobDao
                .modifyJob(
                  BSONDocument(Job.JOBID -> job.jobID),
                  BSONDocument("$set"    -> BSONDocument(Job.CLUSTERDATA -> clusterData, Job.HASH -> jobHash))
                )
                .foreach {
                  case Some(_) =>
                    // Get new runscript instance from the runscript manager
                    val runscript: Runscript = runscriptManager(job.tool).withEnvironment(env)
                    // Load the parameters from the serialized parameters file
                    val params = executionContext.reloadParams
                    // Validate the Parameters (again) to ensure that everything works
                    val validParameters = validatedParameters(job, runscript, params)

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
                        runningExecutions = runningExecutions.updated(job.jobID, executionContext.executeNext.run())
                      }
                    } else {
                      // TODO Implement Me. This specifies what the JobActor should do if not all parameters have been specified
                      log.info("STAY")
                    }

                    self ! JobStateChanged(job.jobID, Prepared)
                  case None => NotUsed
                }
            case None => NotUsed
          }
        case None => NotUsed
      }

    // User Starts watching job
    case AddToWatchlist(jobID, userID) =>
      val _ = jobDao
        .modifyJob(BSONDocument(Job.JOBID -> jobID), BSONDocument("$addToSet" -> BSONDocument(Job.WATCHLIST -> userID)))
        .map {
          case Some(updatedJob) =>
            userSessions
              .modifyUserWithCache(BSONDocument(User.IDDB   -> userID),
                                   BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> jobID)))
              .foreach { _ =>
                currentJobs = currentJobs.updated(jobID, updatedJob)
                val wsActors = wsActorCache.get(userID.stringify): Option[List[ActorRef]]
                wsActors.foreach(_.foreach(_ ! PushJob(updatedJob)))
              }
          case None => NotUsed
        }

    // User does no longer watch this Job (stays in JobManager)
    case RemoveFromWatchlist(jobID, userID) =>
      jobDao
        .modifyJob(BSONDocument(Job.JOBID -> jobID), BSONDocument("$pull" -> BSONDocument(Job.WATCHLIST -> userID)))
        .foreach {
          case Some(updatedJob) =>
            userSessions
              .modifyUserWithCache(BSONDocument(User.IDDB -> userID),
                                   BSONDocument("$pull"   -> BSONDocument(User.JOBS -> jobID)))
              .foreach { _ =>
                currentJobs = currentJobs.updated(jobID, updatedJob)
                val wsActors = wsActorCache.get(userID.stringify): Option[List[ActorRef]]
                wsActors.foreach(_.foreach(_ ! ClearJob(jobID)))
              }
          case None => NotUsed
        }

    // Message from outside that the jobState has changed
    case JobStateChanged(jobID: String, jobState: JobState) =>
      getCurrentJob(jobID).foreach {
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
            updateJobState(job).map { job =>
              //Remove the job from the jobActor
              removeJob(job.jobID)
              // Tell the user that their job finished via eMail (can be either failed or done)
              sendJobUpdateMail(job)
            }
          } else {
            updateJobState(job)
          }
        case None => NotUsed
      }

    // Checks the current jobs against the currently running cluster jobs to see if there are any dead jobs
    case PolledJobs(qStat: QStat) =>
      val clusterJobIDs = qStat.qStatJobs.map(_.sgeID)
      log.info(
        s"[JobActor[$jobActorNumber].PolledJobs] sge Jobs to check: ${clusterJobIDs
          .mkString(", ")}\nactor Jobs to check:${currentJobs.values.flatMap(_.clusterData.map(_.sgeID)).mkString(", ")}"
      )
      currentJobs.values.foreach { job =>
        job.clusterData match {
          case Some(clusterData) =>
            val jobInCluster = clusterJobIDs.contains(clusterData.sgeID)
            log.info(
              s"[JobActor[$jobActorNumber].PolledJobs] Job ${job.jobID} with sgeID ${clusterData.sgeID}: ${if (jobInCluster) "active"
              else "inactive"}"
            )
            if ((!job.isFinished && !jobInCluster) || isOverDue(job) || sgeFailed(clusterData.sgeID, qStat)) {
              self ! JobStateChanged(job.jobID, Error)
            }
          case None => NotUsed
          // also delete
        }
      }

    // Sets the cluster job ID for a job
    case SetSGEID(jobID: String, sgeID: String) =>
      jobDao
        .modifyJob(BSONDocument(Job.JOBID -> jobID), BSONDocument("$set" -> BSONDocument(Job.SGEID -> sgeID)))
        .foreach {
          case Some(job) =>
            currentJobs = currentJobs.updated(job.jobID, job)
          case None => NotUsed
        }

    case UpdateLog =>
      currentJobs.foreach { job =>
        val foundWatchers =
          job._2.watchList.flatMap(userID => wsActorCache.get(userID.stringify): Option[List[ActorRef]])
        job._2.status match {
          case Running => foundWatchers.flatten.foreach(_ ! WatchLogFile(job._2))
          case _       => NotUsed
        }
      }
  }
}

object JobActor {

  case class PrepareJob(
      job: Job,
      params: Map[String, String],
      startJob: Boolean = false,
      isInternalJob: Boolean = false
  )

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
  case class ShowNotification(title: String, body: String)
  case class ShowJobNotification(jobID: String, title: String, body: String)

  // Job Controller receives push message to update the log
  case class UpdateLog(jobID: String)

  // forward filewatching task to ws actor

  case class WatchLogFile(job: Job)

}
