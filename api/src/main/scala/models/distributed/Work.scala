package models.distributed

import models.distributed.FrontendMasterProtocol.UserRequest
import models.jobs.UserJob


// Something for the Worker
case class Work(workID : String, userRequest : UserRequest, userJob : UserJob)


/*
// Update Input of Job with new Params
case class WPrepare(job : UserJob, params : Map[String, String]) extends Task

// Executes the specified job.
case class WStart(job: UserJob) extends Task

// Deletes the Job, such that the working directory will be brutally removed
case class WDelete(job : UserJob) extends Task

// Worker will read the parameters of the job from the working directory and tell them back in a Map
case class WRead(job : UserJob) extends Task

// Worker was asked to convert all provided links between the provided Jobs
case class WConvert(parentUserJob : UserJob, childUserJob : UserJob, links : Seq[Link]) extends Task


// Worker returns this if he has done his deeds
case class WorkResult(workId: String, result: Any) // TODO result really Any?
*/


/*

    // Job Preparation Routine for a new Job
    case FrontendMasterProtocol.PrepWD(toolname, params, startImmediate, job_id_o, newSubmission) =>

      // Determine the Job ID for the Job that was submitted
      val job_id : String = if(newSubmission) {
        checkOrGenerateJobID(job_id_o)
      } else {
        job_id_o.get
      }

      if(userJobs.contains(job_id)) {

        val job = userJobs(job_id)
        job.startImmediate = startImmediate
        worker ! WPrepare(job, params)

      } else {

        // Create a new Job instance
        val job = UserJob(self, toolname, job_id, user_id, Submitted, startImmediate)

        // This is a new Job, so we have to make the status *Submitted explicit*
        job.changeState(Submitted)

        // Make changes to the UserActor Model
        userJobs.put(job_id, job)

        // Put the new job into the Database Mapping
        databaseMapping.put(job_id, jobRefDB.update(DBJob(None, job_id, user_id, job.getState, job.tool.toolname), session_id))
        worker ! WPrepare(job, params)
      }


 */