package actors

import javax.inject.{ Inject, Singleton }

import actors.DatabaseMonitor.DeleteOldUsers
import akka.actor.{ Actor, ActorLogging, Cancellable }
import models.database.statistics.{ StatisticsObject, UserStatistic }
import models.database.users.User
import models.mailing.OldAccountEmail
import modules.db.MongoStore
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.mailer.MailerClient
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{ BSONDateTime, BSONDocument }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Schedules deletions of old listings in the database as well as statistic entries
  * Created by astephens on 03.07.17.
  */
object DatabaseMonitor {
  object DeleteOldUsers
}

@Singleton
final class DatabaseMonitor @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                      implicit val mailerClient: MailerClient,
                                      mongoStore: MongoStore)
    extends Actor
    with ActorLogging {

  private val userDeletionDelay                         = 1 minutes // Sweeps after this time
  private val userDeletionInterval                      = 3 hours // Sweeps in this interval
  private val userDeletingAfterMonths                   = 1 // Deletes regular accounts after this timeframe
  private val userAwaitingRegistrationDeletingAfterDays = 3 // Deletes users awaiting registration after this timeframe (in days)
  private val userLoggedInDeletingAfterMonths           = 24 // Deletes registered accounts after this timeframe
  private val userLoggedInWarningDaysBeforeDeletion     = 14 // Sending an eMail to the user this many days prior to the deletion

  // interval calling the user deletion method automatically
  private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(userDeletionDelay, userDeletionInterval, self, DeleteOldUsers)(context.system.dispatcher)
  }

  /**
    * Function removes old users and eMails registered users who may be deleted soon
    * @param verbose when true, the logger will show the current action
    */
  private def deleteOldUsers(verbose: Boolean = false): Unit = {
    if (verbose)
      Logger.info("[User Deletion] Cleaning up old user data")

    // Generate the dates for user deletion
    // Date at the moment
    val now = DateTime.now()
    // Date from when the regular users should have logged in last
    val regularUserDeletionDate = now.minusMonths(userDeletingAfterMonths)
    // Date from when the user registered and
    val awitingRegistrationUserDeletionDate = now.minusDays(userAwaitingRegistrationDeletingAfterDays)
    // Date the registered user was logged in last
    val registeredUserDeletionDate = now.minusMonths(userLoggedInDeletingAfterMonths)
    // Date the registered user was logged in last plus the days they have to be messaged prior to actual deletion
    val registeredUserDeletionEMailDate = now
      .minusMonths(userLoggedInDeletingAfterMonths)
      .plusDays(userLoggedInWarningDaysBeforeDeletion)
    // Date to delete the Registered account at
    val registeredUserDeletionDateForEmail = now
      .plusDays(userLoggedInWarningDaysBeforeDeletion)
      .withTimeAtStartOfDay()

    if (verbose)
      Logger.info(s"""[User Deletion] Deletion Times:
                                     regular Users:               $regularUserDeletionDate
                                     awaiting registration Users: $awitingRegistrationUserDeletionDate
                                     registered Users:            $registeredUserDeletionDate
                                     registered Users eMail Date: $registeredUserDeletionEMailDate
                                     date found in eMail:         $registeredUserDeletionDateForEmail""")

    // Collect all the accounts which should be deleted
    mongoStore
      .findUsers(
        BSONDocument(
          "$or" ->
          List(
            BSONDocument( // Removing regular users with no privileges
              User.ACCOUNTTYPE   -> User.NORMALUSER,
              User.DATELASTLOGIN -> BSONDocument("$lt" -> BSONDateTime(regularUserDeletionDate.getMillis))
            ),
            BSONDocument( // Removing regular users who await registration
              User.ACCOUNTTYPE   -> User.NORMALUSERAWAITINGREGISTRATION,
              User.DATELASTLOGIN -> BSONDocument("$lt" -> BSONDateTime(awitingRegistrationUserDeletionDate.getMillis))
            ),
            BSONDocument( // Removing registered users with no privileges
              User.ACCOUNTTYPE   -> User.CLOSETODELETIONUSER,
              User.DATEDELETEDON -> BSONDocument("$lt" -> BSONDateTime(now.getMillis))
            )
          )
        )
      )
      .foreach { users =>
        // Get the userIDs for all found users
        val userIDs = users.map(_.userID)
        // Store the deleted users in the user statistics
        mongoStore.getStats.foreach { statisticsObject =>
          val currentDeleted: Int = statisticsObject.userStatistics.currentDeleted + users.count(_.userData.nonEmpty)
          val modifier = BSONDocument(
            "$set" ->
            BSONDocument(
              s"${StatisticsObject.USERSTATISTICS}.${UserStatistic.CURRENTDELETED}" -> currentDeleted
            )
          )
          mongoStore.modifyStats(statisticsObject, modifier)
        }

        // Finally remove the users with their userID
        mongoStore.removeUsers(BSONDocument(User.IDDB -> BSONDocument("$in" -> userIDs))).foreach { writeResult =>
          if (verbose)
            Logger.info(
              s"[User Deletion] Deleting of ${users.length} old users ${if (writeResult.ok) "successful" else "failed"}"
            )
        }
      }

    if (verbose)
      Logger.info("[User Deletion] Checking if there are any old accounts to send the owner an eMail")

    // Find registered user accounts which are close to their deletion time
    mongoStore
      .findUsers(
        BSONDocument(
          User.DATELASTLOGIN -> BSONDocument("$lt" -> BSONDateTime(registeredUserDeletionEMailDate.getMillis)),
          User.ACCOUNTTYPE   -> User.REGISTEREDUSER
        )
      )
      .foreach { users =>
        if (verbose)
          Logger.info(
            s"[User Deletion] ${users.length} registered users with old accounts found.\nSending eMails to users"
          )

        val userIDs = users.map { user =>
          val mail = OldAccountEmail(user, registeredUserDeletionDateForEmail)
          mail.send
          if (verbose)
            Logger.info(
              "[User Deletion] eMail sent to user: " + user.getUserData.nameLogin + " Last login: " + user.dateLastLogin
                .map(_.toString())
                .getOrElse("[no Date]")
            )
          user.userID
        }

        if (verbose)
          Logger.info(s"[User Deletion] All ${userIDs.length} users eMailed.")

        // Set all the eMailed users to "User.CLOSETODELETIONUSER", so that they do not receive another eMail for the same reason
        mongoStore
          .modifyUsers(
            BSONDocument(User.IDDB -> BSONDocument("$in" -> userIDs)),
            BSONDocument(
              "$set" ->
              BSONDocument(
                User.ACCOUNTTYPE   -> User.CLOSETODELETIONUSER,
                User.DATEDELETEDON -> BSONDateTime(registeredUserDeletionDateForEmail.getMillis)
              )
            )
          )
          .foreach { writeResult =>
            if (verbose)
              Logger.info(s"[User Deletion] Writing ${if (writeResult.ok) { "successful" } else { "failed" }}")
          }
      }
  }

  override def preStart(): Unit = {
    Logger.info("Starting Database Monitor")
  }

  override def postStop(): Unit = {
    Tick.cancel()
  }

  override def receive: Receive = {
    // Remove old users
    case DeleteOldUsers => deleteOldUsers(true)
    case _              =>
    // Not implemented
  }
}
