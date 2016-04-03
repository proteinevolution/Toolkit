package models.distributed

import models.graph.Link


/**
  * Created by lzimmermann on 30.03.16.
  */
object FrontendMasterProtocol {


  sealed trait UserRequest {val sessionID : String}
  sealed trait UserRequestPrepare extends UserRequest {
    val jobID : String
    val toolname : String
    val newJob : Boolean}

  // This requests will be forwarded to the Worker such that it can reply immediately to the sender
  sealed trait UserRequestForward extends UserRequest


  /*
   * FROM User/Frontend TO Master
   */

  // Updates the job with the given and the particular tool. If newJob is true, then a new Job Instance
  // for the User will be generated. Otherwise. an existing job will be updated.
  case class Prepare(sessionID : String,
                     jobID : String,
                     toolname : String,
                     params : Map[String, String],
                     newJob : Boolean) extends UserRequestPrepare

  // Like Prepare, but will Execute the Job Immediately after Preparation
  case class PrepareAndStart(sessionID : String,
                             jobID : String,
                             toolname : String,
                             params : Map[String, String],
                             newJob : Boolean) extends UserRequestPrepare


  // Deletes Job entirely
  case class Delete(sessionID : String, jobID : String) extends UserRequest


  // Attach WebSocket to Master
  case class Subscribe(sessionID : String) extends UserRequest

  // Get the userJob
  case class Get(sessionID : String, jobID : String) extends UserRequest

  // Reads the supplied Parameters of a given job and returns them as a map //TODO This is currently not implemented
  case class Read(sessionID : String, jobID : String) extends UserRequestForward

  // Transforms the output of an old job to the input of a new job
  case class Convert(sessionID : String, parentJobID : String, childJobID : String, links : Seq[Link]) extends UserRequest




  /*
   * FROM Master TO Frontend/User
   */
  case object SessionIDUnknown
  case object JobIDAlreadyInUse
  case object JobUnknown
  case object Accepted




  /*
  // Job changed state
  case class UpdateJob(job : UserJob)

  case class PrepWD(toolname : String, params : Map[String, String], startImmediate : Boolean, job_id_o : Option[String],
                    newSubmission : Boolean)

  // Job ID was Invalid
  case object JobIDInvalid

  // Requested a Job with Job ID
  case class GetJob(jobID : String)

  case class GetJobParams(job_id : String)

  case class GetJobView(job_id : String)

  case class DeleteJob(job_id : String)

  case class AppendChildJob(parent_job_id : String, toolname : String, links : Seq[Link])

  case class ClearJob(job_id : String)

  case class Convert(parent_job_id : String, child_job_id : String, links : Seq[Link])

  // Load jobs from the database
  case object LoadJobsFromDB

  // Tells the User to reload their joblist
  case object UpdateJobList

  // Loads a single job from the database and loads it into the user actor
  case class LoadJob(job_id : String)




  // User requested the job widget list
  case object GetJobList
  case class SendJobList(jobSeq : Seq[UserJob])

  // User requested a suggestion
  case class AutoComplete (suggestion : String)
  case class AutoCompleteSend (jobSeq : Seq[DBJob])
 */
}
