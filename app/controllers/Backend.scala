package controllers

import javax.inject.{ Inject, Named, Singleton }

import actors.DatabaseMonitor.{ DeleteOldJobs, DeleteOldUsers }
import akka.actor.ActorRef
import models.UserSessions
import de.proteinevolution.models.database.statistics.{ JobEvent, JobEventLog, StatisticsObject }
import de.proteinevolution.models.database.users.User
import models.tools.ToolFactory
import de.proteinevolution.db.MongoStore
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

import de.proteinevolution.common.LocationProvider
import play.api.Logger
import play.api.cache._
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{ BSONDateTime, BSONDocument }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by zin on 28.07.16.
  */
@Singleton
final class Backend @Inject()(settingsController: Settings,
                              userSessions: UserSessions,
                              mongoStore: MongoStore,
                              @NamedCache("userCache") implicit val userCache: SyncCacheApi,
                              toolFactory: ToolFactory,
                              @Named("DatabaseMonitor") databaseMonitor: ActorRef,
                              implicit val locationProvider: LocationProvider,
                              val reactiveMongoApi: ReactiveMongoApi,
                              messagesApi: MessagesApi,
                              cc: ControllerComponents)
    extends AbstractController(cc)
    with I18nSupport
    with Common {

  //TODO currently working mithril routes for the backend
  def index: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      if (user.isSuperuser) {
        NoCache(Ok(Json.toJson(List("Index Page"))))
      } else {
        NotFound
      }
    }
  }

  def statistics: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      Logger.info("Statistics called. Access " + (if (user.isSuperuser) "granted." else "denied."))
      if (user.isSuperuser) {
        // Get the first moment of the last month as a DateTime object
        val firstOfLastMonth: ZonedDateTime =
          ZonedDateTime.now.minusMonths(1).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1)

        // Grab the current statistics
        Logger.info("Loading Statistics...")
        val stats = mongoStore.getStats

        // Ensure all tools are in the statistics, even if they have not been used yet
        Logger.info("Statistics loaded.... checking for new tools")
        val statsUpdated = stats.map(_.updateTools(toolFactory.values.values.map(_.toolNameShort).toList))

        // Collect the job events up until the first of the last month
        statsUpdated.flatMap { statistics =>
          if (statistics.lastPushed.compareTo(firstOfLastMonth) < 0) {
            mongoStore
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
                Logger.info(
                  "Collected " + jobEventLogs.length + " elements from the job event logs. Last Push: " + statistics.lastPushed
                )
                statistics.addMonthsToTools(
                  jobEventLogs,
                  statistics.lastPushed.plusMonths(1).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1),
                  firstOfLastMonth
                )
              }
              .flatMap { statisticsObject =>
                mongoStore.updateStats(statisticsObject).map {
                  case Some(statisticsObjectUpdated) =>
                    Logger.info(
                      "Successfully pushed statistics for Months: " + statisticsObjectUpdated.datePushed
                        .filterNot(a => statistics.datePushed.contains(a))
                        .mkString(", ")
                    )
                    // TODO add a way to remove the now collected elements from the JobEventLogs
                    NoCache(
                      Ok(Json.toJson(Json.obj("success" -> "new statistics added", "stat" -> statisticsObjectUpdated)))
                    )
                  case None =>
                    Logger
                      .info("Statistics generated, but it seems like the statistics could not be reloaded from the db")
                    NoCache(
                      Ok(
                        Json.toJson(
                          Json.obj("error" -> "could not reload new stats from DB", "stat" -> statisticsObject)
                        )
                      )
                    )
                }
              }
          } else {
            Logger.info("No need to push statistics. Last Push: " + statistics.lastPushed)
            Future.successful(
              NoCache(Ok(Json.toJson(Json.obj("success" -> "old statistics used", "stat" -> statistics))))
            )
          }
        }
      } else {
        Future.successful(NotFound)
      }
    }
  }

  def runUserSweep: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      Logger.info("User deletion called. Access " + (if (user.isSuperuser) "granted." else "denied."))
      if (user.isSuperuser) {
        databaseMonitor ! DeleteOldUsers
        Ok("ok")
      } else {
        NotFound
      }
    }
  }


  def runJobSweep: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      Logger.info("User deletion called. Access " + (if (user.isSuperuser) "granted." else "denied."))
      if (user.isSuperuser) {
        databaseMonitor ! DeleteOldJobs
        Ok("ok")
      } else {
        NotFound
      }
    }
  }

  def cms: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      if (user.isSuperuser) {
        mongoStore.getArticles(-1).map { articles =>
          NoCache(Ok(Json.toJson(articles)))
        }
      } else {
        Future.successful(NotFound)
      }
    }
  }

  def users: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      if (user.isSuperuser) {
        mongoStore.findUsers(BSONDocument(User.USERDATA -> BSONDocument("$exists" -> true))).map { users =>
          NoCache(Ok(Json.toJson(users)))
        }
      } else {
        Future.successful(NotFound)
      }
    }
  }
}
