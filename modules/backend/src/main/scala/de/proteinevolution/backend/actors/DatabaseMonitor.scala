/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.backend.actors

import java.time.ZonedDateTime

import akka.actor.{ Actor, ActorLogging, Cancellable }
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.models.MailTemplate.OldAccountEmail
import de.proteinevolution.backend.actors.DatabaseMonitor.{ DeleteOldJobs, DeleteOldUsers }
import de.proteinevolution.backend.dao.BackendDao
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.jobs.actors.JobActor.Delete
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.jobs.services.JobActorAccess
import de.proteinevolution.statistics.{ StatisticsObject, UserStatistic }
import de.proteinevolution.tel.env.Env
import de.proteinevolution.user.User
import javax.inject.{ Inject, Singleton }
import play.api.libs.mailer.MailerClient
import reactivemongo.bson.{ BSONDateTime, BSONDocument }

import scala.concurrent.ExecutionContext

@Singleton
final class DatabaseMonitor @Inject()(
    backendDao: BackendDao,
    userDao: UserDao,
    jobDao: JobDao,
    jobActorAccess: JobActorAccess,
    constants: ConstantsV2,
    environment: play.Environment,
    env: Env
)(implicit ec: ExecutionContext, mailerClient: MailerClient)
    extends Actor
    with ActorLogging {

  // interval calling the user deletion method automatically
  private val userDeletionScheduler: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(
      constants.userDeletionDelay,
      constants.userDeletionInterval,
      self,
      DeleteOldUsers
    )(context.system.dispatcher)
  }

  // interval calling the user deletion method automatically
  private val jobDeletionScheduler: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(constants.jobDeletionDelay, constants.userDeletionInterval, self, DeleteOldJobs)(
      context.system.dispatcher
    )
  }

  /**
   * Function removes old users and eMails registered users who may be deleted soon
   * @param verbose when true, the log will show the current action
   */
  private def deleteOldUsers(verbose: Boolean): Unit = {
    if (verbose)
      log.info("[User Deletion] Cleaning up old user data")

    // Generate the dates for user deletion
    // Date at the moment
    val now = ZonedDateTime.now
    // Date from when the regular users should have logged in last
    val regularUserDeletionDate =
      now.minusMonths(constants.userDeleting.toLong)
    // Date from when the user registered and
    val awaitingRegistrationUserDeletionDate =
      now.minusDays(constants.userDeletingRegisterEmail.toLong)
    // Date the registered user was logged in last
    val registeredUserDeletionDate =
      now.minusMonths(constants.userDeletingRegistered.toLong)
    // Date the registered user was logged in last plus the days they have to be messaged prior to actual deletion
    val registeredUserDeletionEMailDate =
      now.minusMonths(constants.userDeletingRegistered.toLong).plusDays(constants.userDeletionWarning.toLong)
    // Date to delete the Registered account at
    val registeredUserDeletionDateForEmail =
      now.plusDays(constants.userDeletionWarning.toLong).toLocalDate.atStartOfDay(now.getZone)

    if (verbose)
      log.info(s"""[User Deletion] Deletion Times:
                                     regular Users:               $regularUserDeletionDate
                                     awaiting registration Users: $awaitingRegistrationUserDeletionDate
                                     registered Users:            $registeredUserDeletionDate
                                     registered Users eMail Date: $registeredUserDeletionEMailDate
                                     date found in eMail:         $registeredUserDeletionDateForEmail""")

    // Collect all the accounts which should be deleted
    userDao
      .findUsers(
        BSONDocument(
          "$or" ->
          List(
            BSONDocument( // Removing regular users with no privileges
              User.ACCOUNTTYPE ->
              User.NORMALUSER,
              User.DATELASTLOGIN ->
              BSONDocument("$lt" -> BSONDateTime(regularUserDeletionDate.toInstant.toEpochMilli))
            ),
            BSONDocument( // Removing regular users who await registration
              User.ACCOUNTTYPE ->
              User.NORMALUSERAWAITINGREGISTRATION,
              User.DATELASTLOGIN ->
              BSONDocument("$lt" -> BSONDateTime(awaitingRegistrationUserDeletionDate.toInstant.toEpochMilli))
            ),
            BSONDocument( // Removing registered users with no privileges
              User.ACCOUNTTYPE ->
              User.CLOSETODELETIONUSER,
              User.DATEDELETEDON ->
              BSONDocument("$lt" -> BSONDateTime(now.toInstant.toEpochMilli))
            )
          )
        )
      )
      .foreach { users =>
        // Get the userIDs for all found users
        val userIDs = users.map(_.userID)
        // Store the deleted users in the user statistics
        backendDao.getStats.foreach { statisticsObject =>
          val currentDeleted: Int = statisticsObject.userStatistics.currentDeleted + users.count(_.userData.nonEmpty)
          backendDao.setStatsCurrentDeleted(statisticsObject, currentDeleted)
        }

        // Finally remove the users with their userID
        userDao.removeUsers(BSONDocument(User.IDDB -> BSONDocument("$in" -> userIDs))).foreach { writeResult =>
          if (verbose)
            log.info(
              s"[User Deletion] Deleting of ${users.length} old users ${if (writeResult.ok) "successful" else "failed"}"
            )
        }
      }

    if (verbose)
      log.info("[User Deletion] Checking if there are any old accounts to send the owner an eMail")

    // Find registered user accounts which are close to their deletion time
    userDao
      .findUsers(
        BSONDocument(
          User.DATELASTLOGIN -> BSONDocument(
            "$lt" -> BSONDateTime(registeredUserDeletionEMailDate.toInstant.toEpochMilli)
          ),
          User.ACCOUNTTYPE -> User.REGISTEREDUSER
        )
      )
      .foreach { users =>
        if (verbose)
          log.info(
            s"[User Deletion] ${users.length} registered users with old accounts found.\nSending eMails to users"
          )

        val userIDs = users.map { user =>
          val mail = OldAccountEmail(user, registeredUserDeletionDateForEmail, environment, env)
          mail.send
          if (verbose)
            log.info(
              "[User Deletion] eMail sent to user: " + user.getUserData.nameLogin + " Last login: " + user.dateLastLogin
                .map(_.toString())
                .getOrElse("[no Date]")
            )
          user.userID
        }

        if (verbose)
          log.info(s"[User Deletion] All ${userIDs.length} users eMailed.")

        // Set all the eMailed users to "User.CLOSETODELETIONUSER", so that they do not receive another eMail for the same reason
        userDao
          .modifyUsers(
            BSONDocument(User.IDDB -> BSONDocument("$in" -> userIDs)),
            BSONDocument(
              "$set" ->
              BSONDocument(
                User.ACCOUNTTYPE   -> User.CLOSETODELETIONUSER,
                User.DATEDELETEDON -> BSONDateTime(registeredUserDeletionDateForEmail.toInstant.toEpochMilli)
              )
            )
          )
          .foreach { writeResult =>
            if (verbose)
              log.info(s"[User Deletion] Writing ${if (writeResult.ok) { "successful" } else { "failed" }}")
          }
      }
  }

  private def deleteOldJobs(): Unit = {
    log.info("[Job Deletion] finding old jobs...")
    jobDao.findOldJobs()
      .foreach { jobList =>
        log.info(s"[Job Deletion] found ${jobList.length} jobs for deletion. Sending to job actors.")
        jobList.foreach { job =>
          // Just send a deletion request to the job actor responsible for the job
          jobActorAccess.sendToJobActor(job.jobID, Delete(job.jobID))
        }
      }
  }

  override def preStart(): Unit = {
    log.info("[Database Monitor] starting DB Monitor")
  }

  override def postStop(): Unit = {
    userDeletionScheduler.cancel()
    jobDeletionScheduler.cancel()
    log.info("[Database Monitor] stopping DB Monitor")
  }

  override def receive: Receive = {
    // Remove old users
    case DeleteOldUsers => deleteOldUsers(true)

    // Remove old jobs
    case DeleteOldJobs => deleteOldJobs()

    case _ =>
    // Not implemented
  }
}

object DatabaseMonitor {

  case object DeleteOldUsers

  case object DeleteOldJobs

}
