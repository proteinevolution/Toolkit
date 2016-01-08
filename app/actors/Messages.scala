package actors

import akka.actor.ActorRef

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
case class JobInit(toolname: String, details: String)


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
case class Start(toolname: String, details: String, jobID: Long, userActor: ActorRef)


/**
  * Tells the worker that he should soft stop his computation, so preferably
  * Cancel all Cluster Jobs, tidy the working directory etc.
  */
case class Stop()


/**
The Message that is passed when a new User subscribes to the Job System
 */
object Subscribe
