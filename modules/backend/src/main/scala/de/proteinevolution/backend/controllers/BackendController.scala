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

package de.proteinevolution.backend.controllers

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import akka.actor.{ ActorRef, ActorSystem }
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.backend.actors.DatabaseMonitor.{ DeleteOldJobs, DeleteOldUsers }
import de.proteinevolution.backend.dao.BackendDao
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.message.actors.WebSocketActor.MaintenanceAlert
import de.proteinevolution.tools.ToolConfig
import io.circe.Json
import io.circe.syntax._
import javax.inject.{ Inject, Named, Singleton }
import play.api.Logging
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
final class BackendController @Inject() (
    backendDao: BackendDao,
    userDao: UserDao,
    jobDao: JobDao,
    toolConfig: ToolConfig,
    @Named("databaseMonitor") databaseMonitor: ActorRef,
    actorSystem: ActorSystem,
    cc: ControllerComponents,
    userAction: UserAction
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc)
    with Logging {

  private var maintenanceMode: Boolean = false

  def statistics: Action[AnyContent] = userAction.async { implicit request =>
    logger.info("Statistics called. Access " + (if (request.user.isSuperuser) "granted." else "denied."))
    if (request.user.isSuperuser) {
      // Get the first moment of the last month as a DateTime object
      val firstOfLastMonth: ZonedDateTime =
        ZonedDateTime.now.minusMonths(1).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)

      // Grab the current statistics
      logger.info("Loading Statistics...")
      val stats = backendDao.getStats

      // Ensure all tools are in the statistics, even if they have not been used yet
      logger.info("Statistics loaded.... checking for new tools")
      val statsUpdated = stats.map(_.updateTools(toolConfig.values.values.map(_.toolNameShort).toList))

      // Collect the job events up until the first of the last month
      statsUpdated.flatMap { statistics =>
        if (statistics.lastPushed.compareTo(firstOfLastMonth) < 0) {
          jobDao
            .findJobEventLogs(firstOfLastMonth.toInstant.toEpochMilli)
            .map { jobEventLogs =>
              logger.info(
                "Collected " + jobEventLogs.length + " elements from the job event logs. Last Push: " + statistics.lastPushed
              )
              statistics.addMonthsToTools(
                jobEventLogs,
                statistics.lastPushed.plusMonths(1).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1),
                firstOfLastMonth
              )
            }
            .flatMap { statisticsObject =>
              backendDao.updateStats(statisticsObject).map {
                case Some(statisticsObjectUpdated) =>
                  logger.info(
                    "Successfully pushed statistics for Months: " + statisticsObjectUpdated.datePushed
                      .filterNot(a => statistics.datePushed.contains(a))
                      .mkString(", ")
                  )
                  // TODO add a way to remove the now collected elements from the JobEventLogs
                  NoCache(
                    Ok(
                      Json.obj(
                        "success" -> Json.fromString("new statistics added"),
                        "stat"    -> statisticsObjectUpdated.asJson
                      )
                    )
                  )
                case None =>
                  logger
                    .info("Statistics generated, but it seems like the statistics could not be reloaded from the db")
                  NoCache(
                    Ok(
                      Json.obj(
                        "error" -> Json.fromString("could not reload new stats from DB"),
                        "stat"  -> statisticsObject.asJson
                      )
                    )
                  )
              }
            }
        } else {
          logger.info("No need to push statistics. Last Push: " + statistics.lastPushed)
          fuccess(
            NoCache(Ok(Json.obj("success" -> Json.fromString("old statistics used"), "stat" -> statistics.asJson)))
          )
        }
      }
    } else {
      fuccess(Unauthorized)
    }
  }

  def runUserSweep: Action[AnyContent] = userAction { implicit request =>
    logger.info("User deletion called. Access " + (if (request.user.isSuperuser) "granted." else "denied."))
    if (request.user.isSuperuser) {
      databaseMonitor ! DeleteOldUsers
      NoContent
    } else {
      Unauthorized
    }
  }

  def runJobSweep: Action[AnyContent] = userAction { implicit request =>
    logger.info("User deletion called. Access " + (if (request.user.isSuperuser) "granted." else "denied."))
    if (request.user.isSuperuser) {
      databaseMonitor ! DeleteOldJobs
      NoContent
    } else {
      Unauthorized
    }
  }

  def users: Action[AnyContent] = userAction.async { implicit request =>
    if (request.user.isSuperuser) {
      userDao.findUsersWithInformation().map { users =>
        NoCache(Ok(users.asJson))
      }
    } else {
      fuccess(Unauthorized)
    }
  }

  def getMaintenanceMode: Action[AnyContent] = Action {
      Ok(maintenanceMode.toString)
    }

  def sendMaintenanceAlert(mode: Boolean): Action[AnyContent] = userAction { implicit request =>
    if (request.user.isSuperuser) {
      maintenanceMode = mode
      actorSystem.eventStream.publish(MaintenanceAlert(mode))
      Ok
    } else {
      Unauthorized
    }
  }

}
