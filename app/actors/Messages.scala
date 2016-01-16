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
  * A Message is anything that is meant to go from the Server to the client over the Websocket
 */
case class Message(uuid: String, msg: String)


/**
  * Informs the user whether the Job could be started successfully
  *
  */
case class JobInitStatus(toolname: String, jobID: Long, status: String)


/**
  * Message sent to the JobManager to indicate that the Job is done
  *
  */
case class JobDone(jobID: Long)


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
  * The Message that is passed when a new User subscribes to the Job System
 */
case class SubscribeUser(uid: String)


case class PrepWD(spec: Map[String, Any], toolname: String, uid: String)
case class PrepWDDone(jobID: Long)


case class Prepare(spec: Map[String, Any], jobID: Long, toolname: String, uid: String)


case class JobSubmission(details: String, startJob: Boolean)


/*
Messages which the UserManager will forward to the particular user
 */
sealed trait UserMessage


case class TellUser(uid: String, message: UserMessage)

// TODO Please give me some parameters here
case class Jobinit() extends UserMessage

case class AttachWS(ws: ActorRef) extends UserMessage