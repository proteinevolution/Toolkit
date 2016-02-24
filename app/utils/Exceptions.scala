package utils

import akka.actor.ActorRef

/**
  * Created by lukas on 1/26/16.
  */
object Exceptions {

  case class ActorInitException(message: String, actor: ActorRef) extends Exception(message)
  case class ToolnameNotDistinctException(message : String) extends Exception(message)
}