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

import java.time.{LocalDate}
import java.time.format.DateTimeFormatter

import akka.actor.{ActorRef, ActorSystem}
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.backend.actors.DatabaseMonitor.{DeleteOldJobs, DeleteOldUsers}
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.message.actors.WebSocketActor.MaintenanceAlert
import de.proteinevolution.tools.ToolConfig
import io.circe.Json
import io.circe.syntax._
import javax.inject.{Inject, Named, Singleton}
import play.api.Logging
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import de.proteinevolution.statistics.StatisticsObject

@Singleton
final class BackendController @Inject() (
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

  private var maintenanceSubmitBlocked: Boolean = false
  private var maintenanceMessage: String        = ""

  def runUserSweep: Action[AnyContent] = userAction { implicit request =>
    logger.info(
      "User deletion called. Access " + (if (request.user.isSuperuser)
                                           "granted."
                                         else "denied.")
    )
    if (request.user.isSuperuser) {
      databaseMonitor ! DeleteOldUsers
      NoContent
    } else {
      Unauthorized
    }
  }

  def runJobSweep: Action[AnyContent] = userAction { implicit request =>
    logger.info(
      "User deletion called. Access " + (if (request.user.isSuperuser)
                                           "granted."
                                         else "denied.")
    )
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

  def getMaintenanceState: Action[AnyContent] = Action {
    NoCache(
      Ok(
        Json.obj(
          "message"       -> Json.fromString(maintenanceMessage),
          "submitBlocked" -> Json.fromBoolean(maintenanceSubmitBlocked)
        )
      )
    )
  }

  def setMaintenanceState(): Action[Json] = userAction(circe.json) { implicit request =>
    if (request.user.isSuperuser) {
      request.body.asObject match {
        case None => BadRequest
        case Some(value) =>
          maintenanceMessage = value("message").get.asString.orElse(Some("")).get
          maintenanceSubmitBlocked = value("submitBlocked").get.asBoolean.orElse(Some(false)).get
          actorSystem.eventStream.publish(MaintenanceAlert(maintenanceMessage, maintenanceSubmitBlocked))
          Ok
      }
    } else {
      Unauthorized
    }

  }

  def statistics: Action[AnyContent] = userAction.async { implicit request =>


    val fromDateString = request.getQueryString("fromDate").getOrElse("")
    val fromDate = LocalDate.parse(fromDateString, DateTimeFormatter.ISO_DATE)
    val toDateString = request.getQueryString("toDate").getOrElse("")
    val toDate = LocalDate.parse(toDateString, DateTimeFormatter.ISO_DATE)
    logger.info(fromDate.toString)


    val statisticsObject: StatisticsObject = StatisticsObject(fromDate, toDate)

    logger.info(
      request.toString()
    )

    jobDao
      .findAllJobEventLogs()
      .flatMap { jobEventLogs =>
        logger.info(
          "Collected " + jobEventLogs.length + " elements from the job event logs."
        )
        jobEventLogs.foreach(jobEventLog => {
          statisticsObject.addJobEventLog(jobEventLog)
        })
        Future.successful(
          Ok(statisticsObject.asJson),
        )
      }


  }

}
