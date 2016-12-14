package actors

import javax.inject.{Inject, Named, Singleton}

import actors.JobManager._
import actors.UserManager._
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import controllers.UserSessions
import models.database.{Job, JobState, User}
import modules.{CommonModule, LocationProvider}
import play.api.Logger
import play.api.cache._
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
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
                      implicit val locationProvider: LocationProvider,
                      implicit val materializer     : akka.stream.Materializer)
                           extends Actor
                              with ActorLogging
                              with ReactiveMongoComponents
                              with UserSessions
                              with CommonModule {

  // Maps Session ID to Actor Ref of corresponding WebSocket
  val connectedUsers = new scala.collection.mutable.HashMap[BSONObjectID, ActorRef]

  /**
    * Receive for the Actor
    *
    * @return
    */
  def receive: Receive = LoggingReceive {

    /* User Manager passes Message from Job to all concerned users*/
    case  msg@RunningJobMessage(mainID, message) =>

      findJob(BSONDocument(Job.IDDB -> mainID)).foreach {
        case Some(job) =>
          job.watchList.foreach { user =>
            connectedUsers.get(user) match {
              case Some(userActor) => userActor ! msg
            }
          }
        case None =>
      }


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


      /* TODO I thought about having this case class replacing JobAdded, since JobAdded used JobStateChanged,
       which I do not think is approriate here (After all, the job state has not changed) The message is
       sent in loadJObs in the Service Controller */
    case AddJobWatchList(userID, mainID) =>
      modifyUser(BSONDocument(User.IDDB -> userID), BSONDocument("$push" -> BSONDocument(User.JOBS -> mainID))).foreach {
        case Some(user) =>
          Logger.info("User Watch list was upated to " + user.jobs.mkString)

          updateUserCache(user)
        case None =>
      }


    // Add a Job to the Users view
    case JobAdded(userID : BSONObjectID, job : Job) =>
      modifyUser(BSONDocument(User.IDDB -> userID), BSONDocument("$push" -> BSONDocument(User.JOBS -> job.mainID))).foreach{
        case Some(user) =>
          updateUserCache(user)
          connectedUsers.get(user.userID) match {
            case Some(userActor) =>
              userActor ! JobStateChanged(job, job.status)
            case None =>
          }
        case None =>
      }

    // User is requesting a job to be removed from the view (but not permanently)
    case ClearJob(userID : BSONObjectID, mainID : BSONObjectID) =>
      modifyUser(BSONDocument(User.IDDB -> userID), BSONDocument("$pull" -> BSONDocument(User.JOBS -> mainID))).foreach{
        case Some(user) =>
        case None =>
      }

    /**
      * Messages to Job Manager
      */
    case msg : AddJob =>
      jobManager ! msg
    case msg : ForceDeleteJob =>
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
    case JobStateChanged(job : Job, state : JobState) =>
      for (user <- job.watchList) {
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
  case class JobAdded(userID : BSONObjectID, job : Job) extends MessageWithUserID
  case class ClearJob(userID : BSONObjectID, mainID : BSONObjectID) extends MessageWithUserID

  case class AddJobWatchList(userID : BSONObjectID, mainID : BSONObjectID) extends MessageWithUserID

  // Messages to broadcast to the user
  case class Broadcast(message : String)
  case class MessageUser(userID : BSONObjectID, message : String) extends MessageWithUserID

  case class RunningJobMessage(mainID : BSONObjectID, message : String)
}