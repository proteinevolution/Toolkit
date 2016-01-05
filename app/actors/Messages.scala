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
Just a test Message copied from an Activator example
 */
case class Message(uuid: String, s: String)

/**
The Message that is passed when a new User subscribes to the Job System
 */
object Subscribe
