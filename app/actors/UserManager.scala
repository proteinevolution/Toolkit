package actors

import javax.inject.{Named, Inject, Singleton}
import actors.ESManager.{Search, AutoComplete}
import actors.JobManager._
import actors.UserManager._
import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.event.LoggingReceive
import models.database.{JobState, Job, User}
import modules.Common
import play.api.Logger
import play.api.cache._
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by astephens on 18.08.16.
  */
@Singleton
final class UserManager @Inject() (
@NamedCache("userCache") implicit val userCache        : CacheApi,
                               val reactiveMongoApi : ReactiveMongoApi,
              @Named("jobManager") jobManager       : ActorRef,
               @Named("esManager") esManager        : ActorRef,
                      implicit val materializer     : akka.stream.Materializer)
                           extends Actor
                              with ActorLogging
                              with ReactiveMongoComponents
                              with Common {

  // Maps Session ID to Actor Ref of corresponding WebSocket
  val connectedUsers = new scala.collection.mutable.HashMap[BSONObjectID, ActorRef]

  /**
    * Receive for the Actor
    *
    * @return
    */
  def receive: Receive = LoggingReceive {
    /**
      * Messages to User Manager
      */
    // User Connected, add them to the connected users list
    case UserConnect(userID : BSONObjectID) =>
      //Logger.info("User Connecting: " + userID.stringify)
      val actorRef = connectedUsers.getOrElseUpdate(userID, sender())
      modifyUser(BSONDocument(User.IDDB -> userID), BSONDocument("$set" -> BSONDocument(User.CONNECTED -> true))).foreach{
        case Some(user) =>
        case None =>
      }

    // User Disconnected, Remove them from the connected users list.
    case UserDisconnect(userID : BSONObjectID) =>
      //Logger.info("User Disconnected: " + userID.stringify)
      val actorRef = connectedUsers.remove(userID)
      modifyUser(BSONDocument(User.IDDB -> userID), BSONDocument("$unset" -> BSONDocument(User.CONNECTED -> ""))).foreach{
        case Some(user) =>
        case None =>
      }

    case GetJobList(userID : BSONObjectID) =>
      //Logger.info("Connection stands, fetching jobs")
      findUser(BSONDocument(User.IDDB -> userID)).foreach {
        case Some(user) =>
          // TODO since Common module has a link to all dbs we could just find every job here instead of querying the jobmanager
          jobManager ! FetchJobs(user.userID, user.jobs)
        case None =>
          // TODO this should not happen but we might need to catch unidentified Users who have a Websocket
      }

    // Add a Job to the Users view
    case JobAdded(userID : BSONObjectID, mainID : BSONObjectID) =>
      modifyUser(BSONDocument(User.IDDB -> userID), BSONDocument("$push" -> BSONDocument(User.JOBS -> mainID))).foreach{
        case Some(user) =>
        case None =>
      }

    // User is requesting a job to be removed from the view (but not permanently)
    case ClearJob(userID : BSONObjectID, mainID : BSONObjectID) =>
      modifyUser(BSONDocument(User.IDDB -> userID), BSONDocument("$pull" -> BSONDocument(User.JOBS -> mainID))).foreach{
        case Some(user) =>
        case None =>
      }

    /**
      * Messages to Elastic Search Manager
      */
    case msg : AutoComplete =>
      esManager ! msg

    case msg : Search =>
      esManager ! msg



    /**
      * Messages to Job Manager
      */
    case msg : AddJob =>
      jobManager ! msg
    case msg : DeleteJob =>
      jobManager ! msg
    case msg : StartJob =>
      jobManager ! msg

      
    /**
      * Websocket Messages
      */
    // Send message to all connected users
    case Broadcast(message : String) =>
      for (user <- connectedUsers.values) {
        user ! Broadcast(message)
      }

    // Send job state changed message to all connected users
    case JobStateChanged(job : Job, state : JobState.JobState) =>
      for (user <- job.watchList.getOrElse(List.empty)) {
        connectedUsers.get(user) match {
          case Some(userActor) =>
            userActor ! JobStateChanged (job, state)
          case None =>
            // TODO send the user a Email or give them a message on the page here
        }
      }

    case msg : MessageWithUserID =>
      connectedUsers.get(msg.userID) match {
        case Some(userActor) =>
          userActor ! msg
        case None =>
          // TODO the user might have disconnected while a message was sent
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

  // Get a request to send the job list
  case class GetJobList(userID : BSONObjectID) extends MessageWithUserID
  case class JobAdded(userID : BSONObjectID, mainID : BSONObjectID) extends MessageWithUserID
  case class ClearJob(userID : BSONObjectID, mainID : BSONObjectID) extends MessageWithUserID

  // Messages to broadcast to the user
  case class Broadcast(message : String)
  case class MessageUser(userID : BSONObjectID, message : String) extends MessageWithUserID
}