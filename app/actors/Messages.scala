package actors

import akka.actor.ActorRef
import play.api.libs.json.JsValue

/**
  *
  * This file currently holds all messages that can be passed between actors.
  *
  * Please annotate each message with a descriptive DocString stating the purpose and
  * which actors are involved.
  *
  * Created by lukas on 1/5/16.
  *
  * */

/**
A Message is anything that is meant to go from the Server to the client over the Websocket
 */
case class Message(uuid: String, msg: String)


/**
  * Message the User sends to declare a new Job to the JobManager
  */
case class JobInit(jobID: Long)


/**
  * Informs the user whether the Job could be started successfully
  *
  */
case class JobInitStatus(toolname: String, jobID: Long, status: String)


/**
  * Message sent to the JobManager to indicate that the Job is done
  *
  */
case class JobDone(userActor: ActorRef, toolname: String, details: String, jobID: Long)


/**
  * Starts the JobWorker for one distinguished Job
  */
case class Start(jobID: Long)


/**
  * Tells the worker that he should soft stop his computation, so preferably
  * Cancel all Cluster Jobs, tidy the working directory etc.
  */
case class Stop()


/**
The Message that is passed when a new User subscribes to the Job System
 */
case class SubscribeUser(uid: String)


/**
  * Tells the JobManager to create a new Working Directory for a Job
  *
  * @param details
  * @param startJob whether or not the Job should be started afterwards
  */
case class PrepWD(details: String, jobID: Long, startJob: Boolean)

case class JobSubmission(details: String, startJob: Boolean)
