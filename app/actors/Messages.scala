package actors

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
case class JobInit(uuid: String, toolname: String, details: String)


/**
The Message that is passed when a new User subscribes to the Job System
 */
object Subscribe
