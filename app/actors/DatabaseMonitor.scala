package actors

import javax.inject.{Inject, Singleton}

import actors.DatabaseMonitor.DeleteOldUsers
import akka.actor.{Actor, ActorLogging, Cancellable}
import models.database.users.User
import models.mailing.OldAccountEmail
import modules.db.MongoStore
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.mailer.MailerClient
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDateTime, BSONDocument}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Schedules deletions of old listings in the database
  * Created by astephens on 03.07.17.
  */

object DatabaseMonitor {
  object DeleteOldUsers
}

@Singleton
final class DatabaseMonitor @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                             implicit val mailerClient: MailerClient,
                                          mongoStore: MongoStore) extends Actor with ActorLogging {

  private val userDeletionDelay    = 1 minutes            // Sweeps after this time
  private val userDeletionInterval = 3 hours              // Sweeps in this interval
  private val userDeletingAfterMonths = 1                 // Deletes regular accounts after this timeframe
  private val userAwaitingRegistrationDeletingAfterDays = 3        // Deletes users awaiting registration after this timeframe (in days)
  private val userLoggedInDeletingAfterMonths = 24        // Deletes registered accounts after this timeframe
  private val userLoggedInWarningDaysBeforeDeletion = 14  // Sending an eMail to the user this many days prior to the deletion

  //private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    //context.system.scheduler.schedule(userDeletionDelay, userDeletionInterval, self, DeleteOldUsers)(context.system.dispatcher)
  //}

  override def preStart(): Unit = {
    Logger.info("Starting Database Monitor")
  }

  override def postStop(): Unit = {
    //Tick.cancel()
  }

  override def receive: Receive = {

    // Remove old users
    case DeleteOldUsers =>
      Logger.info("Cleaning up old user data")
      mongoStore.findUsers(
        BSONDocument("$or" -> List(
          BSONDocument(// Removing regular users with no privileges
            User.DATELASTLOGIN ->
              BSONDocument("$lt" -> BSONDateTime(DateTime.now().minusMonths(userDeletingAfterMonths).getMillis)),
            User.ACCOUNTTYPE   ->
              List(User.NORMALUSER)
          ),
          BSONDocument(// Removing regular users who await registration
            User.DATELASTLOGIN ->
              BSONDocument("$lt" -> BSONDateTime(DateTime.now().minusDays(userAwaitingRegistrationDeletingAfterDays).getMillis)),
            User.ACCOUNTTYPE   ->
              List(User.NORMALUSERAWAITINGREGISTRATION)
          ),
          BSONDocument(// Removing registered users with no privileges
            User.DATELASTLOGIN ->
              BSONDocument("$lt" -> BSONDateTime(DateTime.now().minusMonths(userLoggedInDeletingAfterMonths).getMillis)),
            User.ACCOUNTTYPE   ->
              User.CLOSETODELETIONUSER
          ))
      )).foreach{ users =>
        val userIDs = users.map{ user =>
          Logger.info(s"Deleting user: ${user.userID}\nJobs: ${user.jobs.mkString(",")}")
          user.userID
        }
        // TODO need to implement statistics before deleting the accounts?  // Not deleting users just yet
        mongoStore.removeUsers(BSONDocument(User.IDDB -> userIDs)).foreach{ writeResult =>
          Logger.info(s"Deleting old jobs ${if (writeResult.ok) "successful" else "failed"}")
        }
      }

      Logger.info("Checking if there are any old accounts to send the owner an eMail")
      mongoStore.findUsers(BSONDocument(
        User.DATELASTLOGIN -> BSONDocument("$lt"     -> BSONDateTime(DateTime.now()
          .minusMonths(userLoggedInDeletingAfterMonths) //
          .plusDays(userLoggedInWarningDaysBeforeDeletion)
          .getMillis)),
        User.ACCOUNTTYPE   -> User.REGISTEREDUSER
      )).foreach{ users =>
        val userIDs = users.map { user =>
          Logger.info(s"User found: ${user.getUserData.nameLogin}")
          val mail = OldAccountEmail(user, userLoggedInDeletingAfterMonths)
          mail.send
          user
        }
        mongoStore.modifyUser(BSONDocument(User.IDDB -> userIDs), BSONDocument(User.ACCOUNTTYPE -> User.CLOSETODELETIONUSER))
      }

    case _ =>
      // Not implemented
  }
}
