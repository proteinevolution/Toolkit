package de.proteinevolution.backend.controllers

import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import akka.actor.ActorRef
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.backend.actors.DatabaseMonitor.{ DeleteOldJobs, DeleteOldUsers }
import de.proteinevolution.backend.dao.BackendDao
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.common.models.database.statistics.{ JobEvent, JobEventLog }
import de.proteinevolution.common.models.database.users.User
import de.proteinevolution.tools.ToolConfig
import io.circe.Json
import io.circe.syntax._
import javax.inject.{ Inject, Named, Singleton }
import play.api.Logging
import play.api.mvc._
import reactivemongo.bson.{ BSONDateTime, BSONDocument }

import scala.concurrent.ExecutionContext

@Singleton
final class BackendController @Inject()(
    userSessions: UserSessions,
    backendDao: BackendDao,
    userDao: UserDao,
    jobDao: JobDao,
    toolConfig: ToolConfig,
    @Named("databaseMonitor") databaseMonitor: ActorRef,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc)
    with Logging {

  //TODO currently working mithril routes for the backend
  def index: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      if (user.isSuperuser) {
        NoCache(Ok(List("Index Page").asJson))
      } else {
        NotFound
      }
    }
  }

  def statistics: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      logger.info("Statistics called. Access " + (if (user.isSuperuser) "granted." else "denied."))
      if (user.isSuperuser) {
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
              .findJobEventLogs(
                BSONDocument(
                  JobEventLog.EVENTS ->
                  BSONDocument(
                    "$elemMatch" ->
                    BSONDocument(
                      JobEvent.TIMESTAMP ->
                      BSONDocument("$lt" -> BSONDateTime(firstOfLastMonth.toInstant.toEpochMilli))
                    )
                  )
                )
              )
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
                        Json.obj("success" -> Json.fromString("new statistics added"),
                                 "stat"    -> statisticsObjectUpdated.asJson)
                      )
                    )
                  case None =>
                    logger
                      .info("Statistics generated, but it seems like the statistics could not be reloaded from the db")
                    NoCache(
                      Ok(
                        Json.obj("error" -> Json.fromString("could not reload new stats from DB"),
                                 "stat"  -> statisticsObject.asJson)
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
        fuccess(NotFound)
      }
    }
  }

  def runUserSweep: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      logger.info("User deletion called. Access " + (if (user.isSuperuser) "granted." else "denied."))
      if (user.isSuperuser) {
        databaseMonitor ! DeleteOldUsers
        NoContent
      } else {
        NotFound
      }
    }
  }

  def runJobSweep: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      logger.info("User deletion called. Access " + (if (user.isSuperuser) "granted." else "denied."))
      if (user.isSuperuser) {
        databaseMonitor ! DeleteOldJobs
        NoContent
      } else {
        NotFound
      }
    }
  }

  def users: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      if (user.isSuperuser) {
        userDao.findUsers(BSONDocument(User.USERDATA -> BSONDocument("$exists" -> true))).map { users =>
          NoCache(Ok(users.asJson))
        }
      } else {
        fuccess(NotFound)
      }
    }
  }

  def maintenance: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      if (user.isSuperuser) {
        //clusterMonitor ! Multicast TODO put somewhere else
        Ok
      } else {
        NotFound
      }
    }
  }

}
