package actors

import javax.inject.{Named, Inject, Singleton}
import actors.JobManager.{GetJobList, JobStateChanged}
import actors.UserManager._
import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.event.LoggingReceive
import models.database.{JobState, Job}
import reactivemongo.bson.BSONObjectID

/**
  * Created by astephens on 18.08.16.
  */
@Singleton
final class UserManager @Inject() (
                      @Named("jobManager") jobManager  : ActorRef,
                              implicit val materializer: akka.stream.Materializer)
                                   extends Actor
                                      with ActorLogging {

  // Maps Session ID to Actor Ref of corresponding WebSocket
  val connectedUsers = new scala.collection.mutable.HashMap[BSONObjectID, ActorRef]

  def receive: Receive = LoggingReceive {
    // User Connected, add them to the connected users list
    case UserConnect(user : BSONObjectID) =>
      //Logger.info("User Connected: " + userID.stringify)
      val actorRef = connectedUsers.getOrElseUpdate(user, sender())

    // User Disconnected, Remove them from the connected users list.
    case UserDisconnect(user : BSONObjectID) =>
      //Logger.info("User Disconnected: " + userID.stringify)
      val actorRef = connectedUsers.remove(user)

    // Send message to all connected users
    case Broadcast(message : String) =>
      for (user <- connectedUsers.values) {
        user ! Broadcast(message)
      }

    case msg : MessageWithUserID =>
      if (connectedUsers contains msg.userID) {
        connectedUsers.get(msg.userID).get ! msg
      }
  }
}

object UserManager {
  // Trait to mark the object belonging to a User
  trait MessageWithUserID {
    val userID : BSONObjectID
  }

  /**
    * Incoming messages
    */
  // User connect preparation, mediated by WebSocket
  case class UserConnect(userID : BSONObjectID)

  // User disconnect cleanup, mediated by WebSocket
  case class UserDisconnect(userID : BSONObjectID)

  // Messages to broadcast to the user
  case class Broadcast(message : String)
  case class MessageUser(userID : BSONObjectID, message : String) extends MessageWithUserID
}