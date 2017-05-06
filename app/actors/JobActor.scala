package actors

import java.io.{FileOutputStream, ObjectOutputStream}
import javax.inject.{Inject, Named}

import actors.JobActor._
import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive
import models.Constants
import models.database.jobs._
import models.database.statistics.{JobEvent, JobEventLog}
import models.database.users.User
import models.mailing.JobFinishedMail
import models.search.JobDAO
import modules.tel.TEL
import modules.tel.runscripts.{LiteralRepresentation, Representation, _}
import better.files._
import com.typesafe.config.ConfigFactory
import controllers.UserSessions
import models.sge.Qdel
import modules.{CommonModule, LocationProvider}
import modules.tel.env.Env
import modules.tel.execution.{ExecutionContext, RunningExecution, WrapperExecutionFactory}
import modules.tel.runscripts.Runscript.Evaluation
import org.joda.time.DateTime
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.api.libs.mailer.MailerClient
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._

import scala.concurrent.Future


object JobActor {

  case class CreateJob(jobID     : String,
                       user      : User,
                       toolname  : String,
                       params    : Map[String, String])

  case class PrepareJob(job           : Job,
                        params        : Map[String, String],
                        startJob      : Boolean = false,
                        isInternalJob : Boolean = false)

  case class CheckJobHashes(job : Job, params : Map[String, String])

  // Messages the Job Actor to start a job
  case class StartJob(jobID : String, userID : BSONObjectID)

  trait Factory {

    def apply : Actor
  }

  // Messages the jobActor accepts from outside
  case class PushJob(job : Job)

  // Message for the Websocket Actor to send a ClearJob Message
  case class ClearJob(jobID : String)

  // User Actor starts watching
  case class StartWatch(jobID: String, userID: BSONObjectID)

  // UserActor Stops Watching this Job
  case class StopWatch(jobID: String, userID: BSONObjectID)

  // JobActor is requested to Delete the job
  case class Delete(jobID: String, userID : BSONObjectID)

  // Job Controller receives a job state change from the SGE or from any other valid source
  case class JobStateChanged(jobID : String, jobState : JobState)

  // Job Controller receives push message to update the log
  case class UpdateLog(jobID : String)
}

class JobActor @Inject() (runscriptManager        : RunscriptManager, // To get runscripts to be executed
                          env                     : Env, // To supply the runscripts with an environment
                          val reactiveMongoApi    : ReactiveMongoApi,
                 implicit val mailerClient        : MailerClient,
                          val jobDao              : JobDAO,
                          qdel                    : Qdel,
                          wrapperExecutionFactory : WrapperExecutionFactory,
                          implicit val locationProvider : LocationProvider,
                          @Named("jobIDActor") jobIDActor : ActorRef,
                          @NamedCache("userCache") implicit val userCache : CacheApi,
                          @NamedCache("wsActorCache") implicit val wsActorCache : CacheApi) extends Actor with Constants with UserSessions with CommonModule {


  // Attributes asssocidated with a Job
  private var currentJobs: Map[String, Job] = Map.empty
  private var currentJobLogs: Map[String, JobEventLog] = Map.empty
  private var currentExecutionContexts: Map[String, ExecutionContext] = Map.empty

  // Running executions
  private var runningExecutions: Map[String, RunningExecution] = Map.empty

  /**
    * Get the job from the current jobs. If it is not there, get it from the DB.
    * @param jobID
    * @return
    */
  private def getCurrentJob(jobID : String): Future[Option[Job]] = {
    // Check if the job is still in the current jobs.
    this.currentJobs.get(jobID) match {
      case Some(job) => // Everything is fine. Return the job.
        Future.successful(Some(job))
      case None =>      // Job is not in the current jobs.. try to get it back.
        findJob(BSONDocument(Job.JOBID -> jobID)).map {
          case Some(job) =>
            // Get the job back into the current jobs
            this.currentJobs = this.currentJobs.updated(job.mainID.stringify, job)
            // TODO Check if the job is a running job and also if the cluster has done any changes with on the job.
            // Return the job
            Some(job)
          case None =>
            // Something must have went wrong, the job is not in the DB.
            None
        }
    }
  }

  /** Supplies a value for a particular Parameter. Returns params again if the parameter
    * is not present
    *
    * @param name
    * @param value
    * @param params
    */
  private def supply(jobID: String, name: String, value: String, params: Seq[(String, (Runscript.Evaluation, Option[Argument]))])
  : Seq[(String, (Runscript.Evaluation, Option[Argument]))] = {
      params.map  {
        case (paramName, (evaluation, _)) if paramName == name =>
          val x = Some(evaluation(RString(value), this.currentExecutionContexts(jobID)))
          (name, (evaluation, x))
        case q => q
      }
  }

  /**
    * JobActor removes the Job from its maps
    * @param jobID
    */
  private def removeJob(jobID: String) = {
    val job = this.currentJobs(jobID)

    // If the job is in the current jobs remove it
    this.currentJobs = this.currentJobs.filter(_._1 == jobID)

    // Save Job Event Log to the collection and remove it from the map afterwards
    if(this.currentJobLogs.contains(jobID)) {
      addJobLog(this.currentJobLogs(jobID))
      this.currentJobLogs = this.currentJobLogs.filter(_._1 == jobID)
    }

    // If the job Appears in the running Execution, terminate it
    if(this.runningExecutions.contains(jobID)) {
      this.runningExecutions(jobID).terminate()
      this.runningExecutions = this.runningExecutions.filter(_._1 == jobID)
    }
    this.currentExecutionContexts = this.currentExecutionContexts.filter(_._1 == jobID)
  }

  /**
    * Trys to delete a job and inform all watching users about it
    * @param job
    * @param userID
    */
  private def delete(job : Job, userID : BSONObjectID) : Unit = {
    if (job.ownerID.contains(userID)) {
      Logger.info("Owner Requested job Deletion")

      // Message user clients to remove the job from their watchlist
      Logger.info("Informing Users of deletion.")
      val foundWatchers = job.watchList.flatMap(userID => wsActorCache.get(userID.stringify) : Option[List[ActorRef]])
      foundWatchers.flatten.foreach(_ ! ClearJob (job.jobID))

      // Mark the job in mongoDB
      modifyJob(BSONDocument(Job.IDDB      -> job.mainID),
                BSONDocument("$set"        ->
                BSONDocument(Job.DELETION  -> JobDeletion(JobDeletionFlag.OwnerRequest, Some(DateTime.now()))),
                BSONDocument("$unset"      ->
                BSONDocument(Job.WATCHLIST -> "")))).foreach{
        case Some(deletedJob) =>
          Logger.info(s"Job Deletion from DB was successful:\n${deletedJob.toString()}")
        case None =>
          Logger.info("Job Deletion from DB failed.")
      }
    } else {
      // Just clear a job which is not owned by the user
      modifyJob(BSONDocument(Job.IDDB -> job.mainID), BSONDocument("$pull" -> BSONDocument(Job.WATCHLIST -> userID)))
    }
    // clear job from the user's watchlist
    modifyUserWithCache(BSONDocument(User.IDDB -> userID), BSONDocument("$pull" -> BSONDocument(User.JOBS -> job.jobID)))
  }

  /**
    * Updates Jobstate in Model, in database, and notifies user watchlist
    */
  private def updateJobState(job : Job): Future[Job] = {
    // Push the updated job into the current jobs
    this.currentJobs = this.currentJobs.updated(job.jobID, job)

    // Update job in the database and notify watcher upon completion
    modifyJob(BSONDocument(Job.IDDB -> job.mainID),
              BSONDocument("$set"   -> BSONDocument(Job.STATUS -> job.status))).map { modifiedJob =>
      val jobLog = this.currentJobLogs.get(job.jobID) match {
        case Some(jobEventLog) => jobEventLog.addJobStateEvent(job.status)
        case None              => JobEventLog(mainID   = job.mainID,
                                              toolName = job.tool,
                                              events   = List(JobEvent(job.status, Some(DateTime.now))))
      }
      this.currentJobLogs = this.currentJobLogs.updated(job.jobID, jobLog)
      val foundWatchers = job.watchList.flatMap(userID => wsActorCache.get(userID.stringify) : Option[List[ActorRef]])
      foundWatchers.flatten.foreach(_ ! PushJob(job))
      job
    }
  }


  /**
    * Determines whether the parameter list is completely supplied
    *
    * @param params
    * @return
    */
  private def isComplete(params: Seq[(String, (Runscript.Evaluation, Option[Argument]))]) : Boolean  = {
    // If we have an argument for all parameters, we are done
    params.forall( item  => item._2._2.isDefined)
  }

  private def sendJobUpdateMail(job : Job) : Boolean = {
    if (job.emailUpdate && job.ownerID.isDefined) {
      findUser(BSONDocument(User.IDDB -> job.ownerID)).foreach{
        case Some(user) =>
          Logger.info("Sending EMail to owner: " + user.getUserData.eMail)
          val eMail = JobFinishedMail(user, job)
          eMail.send
        case None =>
      }
      true
    } else {
      false
    }
  }


  override def receive = LoggingReceive {

    case PrepareJob(job, params, startJob, isInternalJob) =>
      // TODO Add param validation here

      // set memory allocation for the cluster and let the clusterMonitor define the multiplier
      val h_vmem = (ConfigFactory.load().getString(s"Tools.${job.tool}.memory").dropRight(1).toInt * TEL.memFactor).toString + "G"
      val threads = math.ceil(ConfigFactory.load().getInt(s"Tools.${job.tool}.threads") * TEL.threadsFactor).toInt
      env.configure(s"MEMORY", h_vmem)
      env.configure("THREADS", threads.toString)
      Logger.info(s"${job.jobID} is running with $h_vmem h_vmem")
      Logger.info(s"${job.jobID} is running with $threads threads")

      // jobid will also be available as parameter
      var extendedParams = params + ("jobid" -> job.jobID)

      val clusterData = JobClusterData("", Some(h_vmem), Some(threads))

      // Add job to the current jobs
      this.currentJobs = this.currentJobs.updated(job.jobID, job)

      // Establish execution context for the new Job
      val executionContext = ExecutionContext(jobPath/job.jobID) // TODO check whether directory already exists and catch the resulting error
      this.currentExecutionContexts = this.currentExecutionContexts.updated(job.jobID, executionContext)

      // Create a log for this job
      this.currentJobLogs = this.currentJobLogs.updated(job.jobID,
          JobEventLog(mainID      = job.mainID,
                      toolName    = job.tool,
                      internalJob = isInternalJob,
                      events      = List(JobEvent(job.status, Some(DateTime.now)))))

      // Update the statistics
      increaseJobCount(job.tool) // TODO switch to better statistic handling

      // Get new runscript instance from the runscript manager
      val runscript = runscriptManager(job.tool).withEnvironment(env)
      // Representation of the current State of the job submission
      var parameters : Seq[(String, (Evaluation, Option[Argument]))] = runscript.parameters.map(t => t._1 -> (t._2 -> Some(ValidArgument(new LiteralRepresentation(RString("false"))))))
      for((paramName, value) <- extendedParams) {
        parameters  = supply(job.jobID, paramName, value, parameters)
      }
      // adds the params of the disabled controls from formData, sets value of those to "false"
      for(name <- parameters.map(t => t._1)) {
        if(!(extendedParams contains name)) {
          extendedParams = extendedParams + (name -> "false")
        }
      }

      // Serialize the JobParameters to the JobDirectory
      // Store the extended Parameters in the working directory for faster reloading
      // TODO Use ExecutionContext for file access
      (jobPath/job.jobID/serializedParam).createIfNotExists(asDirectory = false)
      val oos = new ObjectOutputStream(new FileOutputStream((jobPath/job.jobID/serializedParam).pathAsString))
      oos.writeObject(extendedParams)
      oos.close()

      if(isComplete(parameters)) {
        val pendingExecution = wrapperExecutionFactory.getInstance(runscript(parameters.map(t => (t._1, t._2._2.get.asInstanceOf[ValidArgument]))))
        executionContext.accept(pendingExecution)

        this.runningExecutions = this.runningExecutions.updated(job.jobID, executionContext.executeNext.run())
        env.remove(s"MEMORY")
        env.remove(s"THREADS")

      } else {
        // TODO Implement Me. This specifies what the JobActor should do if not all parameters have been specified
        Logger.info("STAY")
      }

      if (startJob) {
        self ! JobStateChanged(job.jobID, Prepared)
      } else {
        self ! JobStateChanged(job.jobID, Pending)
      }

    /**
      * Checks the jobHashDB for matches and generates one for the job if there are none.
      */
    case CheckJobHashes(job, params) =>
      // filter unique parameters
      val paramsWithoutMainID = params - Job.ID - Job.IDDB - Job.JOBID - Job.EMAILUPDATE - "public"

      // Create the job Hash depending on what db is used
      val jobHash = {
        params match {
          case x if x isDefinedAt "standarddb" =>
            val STANDARDDB = (env.get("STANDARD") + "/" + params.getOrElse("standarddb","")).toFile

            JobHash(mainID        = job.mainID,
                    inputHash     = jobDao.generateHash(paramsWithoutMainID).toString(),
                    runscriptHash = jobDao.generateRSHash(job.tool),
                    dbName        = Some("standarddb"),
                    dbMtime       = Some(STANDARDDB.lastModifiedTime.toString),
                    toolname      = job.tool,
                    toolHash      = jobDao.generateToolHash(job.tool),
                    dateCreated   = job.dateCreated,
                    jobID         = job.jobID)

          case x if x isDefinedAt "hhsuitedb" =>
            val HHSUITEDB  = env.get("HHSUITE").toFile

            JobHash(mainID        = job.mainID,
                    inputHash     = jobDao.generateHash(paramsWithoutMainID).toString(),
                    runscriptHash = jobDao.generateRSHash(job.tool),
                    dbName        = Some("hhsuitedb"),
                    dbMtime       = Some(HHSUITEDB.lastModifiedTime.toString),
                    toolname      = job.tool,
                    toolHash      = jobDao.generateToolHash(job.tool),
                    dateCreated   = job.dateCreated,
                    jobID         = job.jobID)

          case x if x isDefinedAt "hhblitsdb" =>
            val HHBLITSDB = env.get("HHBLITS").toFile

            JobHash(mainID        = job.mainID,
                    inputHash     = jobDao.generateHash(paramsWithoutMainID).toString(),
                    runscriptHash = jobDao.generateRSHash(job.tool),
                    dbName        = Some("hhblitsdb"),
                    dbMtime       = Some(HHBLITSDB.lastModifiedTime.toString),
                    toolname      = job.tool,
                    toolHash      = jobDao.generateToolHash(job.tool),
                    dateCreated   = job.dateCreated,
                    jobID         = job.jobID)

          case _ =>
            JobHash(mainID        = job.mainID,
                    inputHash     = jobDao.generateHash(paramsWithoutMainID).toString(),
                    runscriptHash = jobDao.generateRSHash(job.tool),
                    dbName        = Some("none"), // field must exist so that elasticsearch can do a bool query on multiple fields
                    dbMtime       = Some("1970-01-01T00:00:00Z"), // use unix epoch time
                    toolname      = job.tool,
                    toolHash      = jobDao.generateToolHash(job.tool),
                    dateCreated   = job.dateCreated,
                    jobID         = job.jobID)
        }
      }
      // Match the hash
      jobDao.matchHash(jobHash).map { richSearchResponse =>
        Logger.info("Retrieved richSearchResponse")
        println("success: " + richSearchResponse)
        println("hits: " + richSearchResponse.totalHits)

        // Generate a list of hits and convert them into a list of future option jobs
        val mainIDs = richSearchResponse.getHits.getHits.toList.map { hit =>
          BSONObjectID.parse(hit.getId).getOrElse(BSONObjectID.generate()) // Not optimal, as a fake Object ID is generated, but apply(id : String) was deprecated
        }

        // Find the Jobs in the Database
        findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> mainIDs))).map { jobList =>
          val foundMainIDs = jobList.map(_.mainID)
          val unFoundMainIDs = mainIDs.filterNot(checkMainID => foundMainIDs contains checkMainID)
          val jobsPartition = jobList.partition(_.status == Error)

          // Delete index-zombie jobs
          unFoundMainIDs.foreach { mainID =>
            println("[WARNING]: job in index but not in database: " + mainID.stringify)
            jobDao.deleteJob(mainID.stringify)
          }

          jobsPartition._2.lastOption match {
            case Some(matchingJob) =>
            case None =>
              hashCollection.flatMap(_.insert(jobHash))
          }
        }
      }

    case CreateJob(jobID, user, toolname, params) =>
      // TODO Add param validation here
      // set memory allocation on the cluster and let the clusterMonitor define the multiplier

      val h_vmem = (ConfigFactory.load().getString(s"Tools.$toolname.memory").dropRight(1).toInt * TEL.memFactor).toString + "G"
      val threads = math.ceil(ConfigFactory.load().getInt(s"Tools.$toolname.threads") * TEL.threadsFactor).toInt
      env.configure(s"MEMORY", h_vmem)
      env.configure("THREADS", threads.toString)
      Logger.info(s"$jobID is running with $h_vmem h_vmem")
      Logger.info(s"$jobID is running with $threads threads")

      // Get the current date to set it for all three dates
      val jobCreationTime = DateTime.now()

      // jobid will also be available as parameter
      var extendedParams = params + ("jobid" -> jobID)

      val clusterData = JobClusterData("", Some(h_vmem), Some(threads))

      // Set private or public
      val ownerOption = if(params.get("public").isEmpty) { Some(user.userID) } else { None }

      // Make a new JobObject and set the initial values
      val job = Job(mainID      = BSONObjectID.generate(),
                    jobID       = jobID,
                    ownerID     = ownerOption,
                    //project     = Some(BSONObjectID.generate()),
                    status      = Submitted,
                    emailUpdate = params.get(Job.EMAILUPDATE).isDefined,
                    tool        = toolname,
                    clusterData = Some(clusterData),
                    label       = params.get("label"),
                    watchList   = List(user.userID),
                    dateCreated = Some(jobCreationTime),
                    dateUpdated = Some(jobCreationTime),
                    dateViewed  = Some(jobCreationTime))
      this.currentJobs = this.currentJobs.updated(jobID, job)

      // Add job to database
      insertJob(job)

      Logger.info("Job Database insert request done")
      // filter unique parameters
      val paramsWithoutMainID = params - Job.ID - Job.IDDB - Job.JOBID - Job.EMAILUPDATE - "public"

      // get hold of the database in use
      val DBNAME = params match {
        case x if x isDefinedAt "standarddb" => params.getOrElse("standarddb", "")
        case x if x isDefinedAt "hhblitsdb"  => params.getOrElse("hhblitsdb", "")
        case x if x isDefinedAt "hhsuitedb"  => params.getOrElse("hhsuitedb", "")
        case _ => ""
      }

      val STANDARDDB = (env.get("STANDARD") + "/" + params.getOrElse("standarddb","")).toFile
      val HHBLITSDBMTIME = env.get("HHBLITS").toFile.lastModifiedTime.toString
      val HHSUITEDBMTIME = env.get("HHSUITE").toFile.lastModifiedTime.toString

      val jobHash = {
      params match {
        case x if x isDefinedAt "standarddb" => JobHash( mainID = job.mainID,
          jobDao.generateHash(paramsWithoutMainID).toString(),
          jobDao.generateRSHash(toolname),
          dbName = Some(DBNAME),
          dbMtime = Some(STANDARDDB.lastModifiedTime.toString),
          toolname = toolname,
          jobDao.generateToolHash(toolname),
          dateCreated = Some(jobCreationTime),
          jobID = jobID
        )
        case x if x isDefinedAt "hhsuitedb" => JobHash( mainID = job.mainID,
          jobDao.generateHash(paramsWithoutMainID).toString(),
          jobDao.generateRSHash(toolname),
          dbName = Some(DBNAME),
          dbMtime = Some(HHSUITEDBMTIME),
          toolname = toolname,
          jobDao.generateToolHash(toolname),
          dateCreated = Some(jobCreationTime),
          jobID = jobID
        )
        case x if x isDefinedAt "hhblitsdb" => JobHash( mainID = job.mainID,
          jobDao.generateHash(paramsWithoutMainID).toString(),
          jobDao.generateRSHash(toolname),
          dbName = Some(DBNAME),
          dbMtime = Some(HHBLITSDBMTIME),
          toolname = toolname,
          jobDao.generateToolHash(toolname),
          dateCreated = Some(jobCreationTime),
          jobID = jobID
        )
        case _ => JobHash( mainID = job.mainID,
          jobDao.generateHash(paramsWithoutMainID).toString(),
          jobDao.generateRSHash(toolname),
          dbName = Some("none"), // field must exist so that elasticsearch can do a bool query on multiple fields
          dbMtime = Some("1970-01-01T00:00:00Z"), // use unix epoch time
          toolname = toolname,
          jobDao.generateToolHash(toolname),
          dateCreated = Some(jobCreationTime),
          jobID = jobID)
        }
      }
      hashCollection.flatMap(_.insert(jobHash))

      // Add Job to user in database
      modifyUserWithCache(BSONDocument(User.IDDB -> user.userID), BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> jobID)))

      // Establish exection context for the newJob
      val executionContext = ExecutionContext(jobPath/jobID) // TODO check whether directory already exists and catch the resulting error
      this.currentExecutionContexts = this.currentExecutionContexts.updated(jobID, executionContext)

      // Create a log for this job
      // TODO may want to use a different way to identify our users
      val isFromInstitute = user.getUserData.eMail.matches(".+@tuebingen.mpg.de")
      this.currentJobLogs = this.currentJobLogs.updated(job.jobID,
                                                        JobEventLog(mainID      = job.mainID,
                                                                    toolName    = job.tool,
                                                                    internalJob = isFromInstitute,
                                                                    events      = List(JobEvent(job.status, Some(DateTime.now)))))

      // Update the statistics
      increaseJobCount(job.tool) // TODO switch to better statistic handling

      // Get new runscript instance from the runscript manager
      val runscript = runscriptManager(toolname).withEnvironment(env)
      // Representation of the current State of the job submission
      var parameters : Seq[(String, (Evaluation, Option[Argument]))] = runscript.parameters.map(t => t._1 -> (t._2 -> Some(ValidArgument(new LiteralRepresentation(RString("false"))))))
      for((paramName, value) <- extendedParams) {
        parameters  = supply(jobID, paramName, value, parameters)
      }
      // adds the params of the disabled controls from formData, sets value of those to "false"
      for(name <- parameters.map(t => t._1)) {
        if(!(extendedParams contains name)) {
          extendedParams = extendedParams + (name -> "false")
        }
      }

      // Serialize the JobParameters to the JobDirectory
      // Store the extended Parameters in the working directory for faster reloading
      // TODO Use ExecutionContext for file access
      (jobPath/jobID/serializedParam).createIfNotExists(asDirectory = false)
      val oos = new ObjectOutputStream(new FileOutputStream((jobPath/jobID/serializedParam).pathAsString))
      oos.writeObject(extendedParams)
      oos.close()

      if(isComplete(parameters)) {
        val pendingExecution = wrapperExecutionFactory.getInstance(runscript(parameters.map(t => (t._1, t._2._2.get.asInstanceOf[ValidArgument]))))
        executionContext.accept(pendingExecution)

        this.runningExecutions = this.runningExecutions.updated(jobID, executionContext.executeNext.run())
        env.remove(s"MEMORY")
        env.remove(s"THREADS")

      } else {
        // TODO Implement Me. This specifies what the JobActor should do if not all parameters have been specified
        Logger.info("STAY")
      }

    /**
      * Checks everything and deletes the Job.
      * Includes: Removing the job from the cluster if the job is still in the current jobs
      *           Creating the job deleted object and inserting it to the database
      *           Removing the job from ES
      */
    case Delete(jobID, userID) =>
      Logger.info(s"Received Delete for $jobID")
      this.getCurrentJob(jobID).map {
        case Some(job) =>
          Logger.info("Removing Job from Elastic Search.")
          jobDao.deleteJob(job.mainID.stringify) // Remove job from elastic search
          Logger.info("Removing Job from current Jobs.")
          this.removeJob(jobID)
          Logger.info("Removing Job from DB")
          this.delete(job, userID)
          Logger.info("Deletion Complete.")
        case None =>
          Logger.info("No such jobID found in current jobs. Loading job from DB.")
          findJob(BSONDocument(Job.JOBID -> jobID)).map{
            case Some(job) =>
              Logger.info("Found Job in DB. Deleting.")
              Logger.info("Removing Job from Elastic Search.")
              jobDao.deleteJob(job.mainID.stringify) // Remove job from elastic search
              Logger.info("Removing Job from DB")
              this.delete(job, userID)
            case None =>
              Logger.info("No such jobID found in Database. Ignoring.")
          }
      }

    /**
      * Starts the job
      */
    case StartJob(jobID, userID) =>
      this.getCurrentJob(jobID).map {
        case Some(job) =>
          this.currentJobs = this.currentJobs.updated(job.jobID, job)

          // set memory allocation on the cluster and let the clusterMonitor define the multiplier

          val h_vmem = (ConfigFactory.load().getString(s"Tools.${job.tool}.memory").dropRight(1).toInt * TEL.memFactor).toString + "G"
          val threads = math.ceil(ConfigFactory.load().getInt(s"Tools.${job.tool}.threads") * TEL.threadsFactor).toInt
          env.configure(s"MEMORY", h_vmem)
          env.configure("THREADS", threads.toString)
          Logger.info(s"$jobID is running with $h_vmem h_vmem")
          Logger.info(s"$jobID is running with $threads threads")

          val clusterData = JobClusterData("", Some(h_vmem), Some(threads))

          modifyJob(BSONDocument(Job.IDDB -> job.mainID),
                    BSONDocument("$set"   ->
                    BSONDocument(Job.CLUSTERDATA -> clusterData))).foreach{
            case Some(_) =>
              self ! JobStateChanged(job.jobID, Prepared)
            case None =>
              Logger.info("Job could not be written to DB: " + jobID)
          }
        case None =>
          Logger.info("Job not found in DB: " + jobID)
      }


    // User does no longer watch this Job
    case StopWatch(jobID, userID) =>
      modifyJob(BSONDocument(Job.JOBID -> jobID),
                BSONDocument("$pull"   -> BSONDocument(Job.WATCHLIST -> userID))).foreach {
        case Some(updatedJob) =>
          this.currentJobs = this.currentJobs.updated(jobID, updatedJob)
        case None =>
      }
      val wsActors = wsActorCache.get(userID.stringify) : Option[List[ActorRef]]
      wsActors.foreach(_.foreach(_ ! ClearJob(jobID)))

      modifyUserWithCache(BSONDocument(User.IDDB -> userID),
                          BSONDocument("$pull"   -> BSONDocument(User.JOBS -> jobID)))

    // User Starts watching job
    case StartWatch(jobID, userID) =>
      modifyJob(BSONDocument(Job.JOBID   -> jobID),
                BSONDocument("$addToSet" -> BSONDocument(Job.WATCHLIST -> userID))).map {
        case Some(updatedJob) =>
          this.currentJobs = this.currentJobs.updated(jobID, updatedJob)
        case None =>
      }

      modifyUserWithCache(BSONDocument(User.IDDB   -> userID),
                          BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> jobID)))

    // Message from outside that the jobState has changed
    case JobStateChanged(jobID : String, jobState : JobState) =>
      this.getCurrentJob(jobID).map {
        case Some(oldJob) =>
          // Update the job object
          val job = oldJob.copy(status = jobState)
          // Give a update message to all
          Logger.info("Jobstate has changed to " + job.status.toString + " of Job with ID " + job.jobID)

          // Dependent on the state, we have to do different things
          job.status match {
            case Done =>
              // Job is no longer running
              Logger.info("Removing execution context")
              this.runningExecutions = this.runningExecutions.-(job.jobID)
              Logger.info("DONE Removing execution context")
              // Tell the user that their job finished via eMail
              sendJobUpdateMail(job)

              // Put the result files into the database, JobActor has to wait until this process has finished
              Future.sequence((jobPath / job.jobID / "results").list.withFilter(_.hasExtension).withFilter(_.extension.get == ".json").map { file =>
                result2Job(job.jobID, file.nameWithoutExtension, Json.parse(file.contentAsString))
              }).map { _ =>
                // Now we can update the JobState and remove it, once the update has completed
                this.updateJobState(job).map { job =>
                  this.removeJob(job.jobID)
                  Logger.info("Job has been removed from JobActor")
                }
              }

            // Currently no further error handling
            case Error =>
              this.updateJobState(job).map { job =>
                this.removeJob(job.jobID)

                // Tell the user that their job failed via eMail
                sendJobUpdateMail(job)

                // Update the statistics for the failed job TODO - swap to better statistic handling
                increaseJobCount(job.tool, failed = true)
              }

            case _ =>
              this.updateJobState(job)
          }
        case None =>
          Logger.info("Job not found: " + jobID)

      }

    case UpdateLog(jobID : String) =>
      currentJobs.get(jobID) match {
        case Some(job) =>
          val foundWatchers = job.watchList.flatMap(userID => wsActorCache.get(userID.stringify) : Option[List[ActorRef]])
          foundWatchers.flatten.foreach(_ ! PushJob(job))
        case None =>
      }
  }
}
