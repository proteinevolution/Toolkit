package actors

import javax.inject.{Named, Inject, Singleton}
import actors.ESManager.{Search, AutoComplete}
import actors.JobManager._
import actors.UserManager._
import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.event.LoggingReceive
import models.database.{JobState, Job, User}
import play.api.Logger
import play.api.cache._
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by astephens on 18.08.16.
  */
@Singleton
final class UserManager @Inject() (
          @NamedCache("userCache") userCache        : CacheApi,
                               val reactiveMongoApi : ReactiveMongoApi,
              @Named("jobManager") jobManager       : ActorRef,
               @Named("esManager") esManager        : ActorRef,
                      implicit val materializer     : akka.stream.Materializer)
                           extends Actor
                              with ActorLogging
                              with ReactiveMongoComponents {

  def userCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))


  // Maps Session ID to Actor Ref of corresponding WebSocket
  val connectedUsers = new scala.collection.mutable.HashMap[BSONObjectID, ActorRef]

  /**
    * Finds an user object in the database
    *
    * @param userID ID of the user
    * @return
    */
  def findUser(userID : BSONObjectID) = {
    userCollection.flatMap(_.find(BSONDocument(User.IDDB -> userID)).one[User])
  }

  /**
    * Adds an user object to the database
    *
    * @param user ID of the user
    * @return
    */
  def insertUser(user : User) = {
    userCollection.foreach(_.insert(user))
  }

  /**
    * Updates the User object in the database with a modifier or with a replacement object
    *
    * @param userID ID of the user
    * @param modifier User Object or Modifier
    * @return
    */
  def updateUser(userID : BSONObjectID, modifier: BSONDocument) = {
    userCollection.foreach(_.update(BSONDocument(User.IDDB -> userID), modifier))
  }

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
      updateUser(userID, BSONDocument("$set" -> BSONDocument("up" -> true)))

    // User Disconnected, Remove them from the connected users list.
    case UserDisconnect(userID : BSONObjectID) =>
      //Logger.info("User Disconnected: " + userID.stringify)
      val actorRef = connectedUsers.remove(userID)
      updateUser(userID, BSONDocument("$set" -> BSONDocument("up" -> false)))

    case GetJobList(userID : BSONObjectID) =>
      //Logger.info("Connection stands, fetching jobs")
      findUser(userID).foreach {
        case Some(user) =>
          jobManager ! FetchJobs(user.userID, user.jobs)
        case None =>
          // TODO this should not happen but we might need to catch unidentified Users who have a Websocket
      }

    // Add a Job to the Users view
    case JobAdded(userID : BSONObjectID, mainID : BSONObjectID) =>
      updateUser(userID, BSONDocument("$push" -> BSONDocument(User.JOBS -> mainID)))

    // User is requesting a job to be removed from the view (but not permanently)
    case ClearJob(userID : BSONObjectID, mainID : BSONObjectID) =>
      updateUser(userID, BSONDocument("$pull" -> BSONDocument(User.JOBS -> mainID)))

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