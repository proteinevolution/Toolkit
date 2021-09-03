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

import akka.actor.{ Actor, ActorLogging, Cancellable }
import better.files._
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.models.MailTemplate.OldAccountEmail
import de.proteinevolution.backend.actors.DatabaseMonitor.{ DeleteOldJobs, DeleteOldUsers }
import de.proteinevolution.backend.dao.BackendDao
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.jobs.dao.JobDao
import play.api.Configuration
import play.api.libs.mailer.MailerClient

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

@Singleton
final class DatabaseMonitor @Inject() (
    backendDao: BackendDao,
    userDao: UserDao,
    jobDao: JobDao,
    constants: ConstantsV2,
    config: Configuration
)(implicit ec: ExecutionContext, mailerClient: MailerClient)
    extends Actor
    with ActorLogging {

  // interval calling the user deletion method automatically
  private val userDeletionScheduler: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.scheduleWithFixedDelay(
      constants.userDeletionDelay,
      constants.userDeletionInterval,
      self,
      DeleteOldUsers
    )(context.system.dispatcher)
  }

  // interval calling the user deletion method automatically
  private val jobDeletionScheduler: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.scheduleWithFixedDelay(
      constants.jobDeletionDelay,
      constants.jobDeletionInterval,
      self,
      DeleteOldJobs
    )(
      context.system.dispatcher
    )
  }

  /**
   * Function removes old users and eMails registered users who may be deleted soon
   *
   * @param verbose
   *   when true, the log will show the current action
   */
  private def deleteOldUsers(verbose: Boolean): Unit = {
    if (verbose)
      log.info("[User Deletion] Cleaning up old users")

    // Collect all the accounts which should be deleted
    userDao.findOldUsers().foreach { users =>
      // Get the userIDs for all found users
      val userIDs = users.map(_.userID)
      // Store the deleted users in the user statistics
      backendDao.getStats.foreach { statisticsObject =>
        val currentDeleted: Int = statisticsObject.userStatistics.currentDeleted + users.count(_.isRegistered)
        backendDao.setStatsCurrentDeleted(statisticsObject, currentDeleted)
      }

      // Finally remove the users with their userID
      userDao.removeUsers(userIDs).onComplete {
        case Failure(exception) =>
          if (verbose)
            log.info(s"[User Deletion] Deleting of ${users.length} old users failed with error: $exception")
        case Success(_) =>
          if (verbose)
            log.info(s"[User Deletion] Deleting of ${users.length} old users successful")
      }
    }

    if (verbose)
      log.info("[User Deletion] Checking if there are any old accounts to warn with an email")

    // Find registered user accounts which are close to their deletion time
    userDao.findUsersToWarn().foreach { users =>
      if (verbose)
        log.info(
          s"[User Deletion] ${users.length} registered users with old accounts found.\nSending eMails to users"
        )

      val userIDs = users.map { user =>
        OldAccountEmail(user, constants.userDeletionWarning, config).send
        if (verbose)
          log.info(
            "[User Deletion] eMail sent to user: " + user.userData.get.nameLogin + " Last login: " + user.dateLastLogin.toString
          )
        userDao.setDeletionWarningSent(user.userID)
        user.userID
      }

      if (verbose)
        log.info(s"[User Deletion] All ${userIDs.length} users emailed.")

      userDao
    }
  }

  private def deleteOldJobs(): Unit = {
    log.info("[Job Deletion] Cleaning up old jobs")
    jobDao.findOldJobs().foreach { jobs =>
      // Get the userIDs for all found users
      val jobIDs = jobs.map(_.jobID)

      jobIDs.foreach { jobID =>
        s"${constants.jobPath}$jobID".toFile.delete(swallowIOExceptions = true)
      }

      jobDao.removeJobs(jobIDs).onComplete {
        case Failure(exception) =>
          log.info(s"[Job Deletion] Deleting of ${jobs.length} old jobs failed with error: $exception")
        case Success(_) =>
          log.info(s"[Job Deletion] Deleting of ${jobs.length} old jobs successful")
      }

      userDao.removeJobs(jobIDs).onComplete {
        case Failure(exception) =>
          log.info(s"[Job Deletion] Removing ${jobs.length} old jobs from user's jobs failed with error: $exception")
        case Success(_) =>
          log.info(s"[Job Deletion] Removing ${jobs.length} old jobs from user's jobs successful")
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
