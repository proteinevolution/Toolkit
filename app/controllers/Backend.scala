package controllers

import javax.inject.{Inject, Singleton}

import models.UserSessions
import models.database.statistics.{JobEvent, JobEventLog, StatisticsObject}
import models.database.users.User
import models.tools.ToolFactory
import modules.LocationProvider
import modules.db.MongoStore
import org.joda.time.{DateTime, DateTimeZone}
import play.api.Logger
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDateTime, BSONDocument}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by zin on 28.07.16.
  */
@Singleton
final class Backend @Inject()(webJarAssets: WebJarAssets,
                              settingsController: Settings,
                              userSessions: UserSessions,
                              mongoStore: MongoStore,
                              @NamedCache("userCache") implicit val userCache: CacheApi,
                              toolFactory: ToolFactory,
                              implicit val locationProvider: LocationProvider,
                              val reactiveMongoApi: ReactiveMongoApi,
                              val messagesApi: MessagesApi)
    extends Controller
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
        val firstOfLastMonth: DateTime = DateTime.now().withZone(DateTimeZone.getDefault).minusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay()

        // Grab the current statistics
        Logger.info("Loading Statistics...")
        val stats = mongoStore.getStats.map(_.getOrElse(StatisticsObject()))

        // Ensure all tools are in the statistics, even if they have not been used yet
        Logger.info("Statistics loaded.... checking for new tools")
        val statsUpdated = stats.map(_.updateTools(toolFactory.values.values.map(_.toolNameShort).toList))

        // Collect the job events up until the first of the last month
        statsUpdated.flatMap { statistics =>
          if (statistics.lastPushed.compareTo(firstOfLastMonth) < 0) {
            mongoStore.findJobEventLogs(
              BSONDocument(
                JobEventLog.EVENTS ->
                  BSONDocument(
                    "$elemMatch" ->
                      BSONDocument(
                        JobEvent.TIMESTAMP ->
                          BSONDocument("$lt" -> BSONDateTime(firstOfLastMonth.getMillis))
                      )
                  )
              )
            ).map { jobEventLogs =>
              Logger.info("Collected " + jobEventLogs.length + " elements from the job event logs. Last Push: " + statistics.lastPushed)
              statistics.addMonthsToTools(jobEventLogs, statistics.lastPushed.withZone(DateTimeZone.getDefault).plusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay(), firstOfLastMonth)
            }.flatMap { statisticsObject =>
              mongoStore.updateStats(statisticsObject).map {
                case Some(statisticsObjectUpdated) =>
                  Logger.info("Successfully pushed statistics for Months: " + statisticsObjectUpdated.datePushed.filterNot(a => statistics.datePushed.contains(a)).mkString(", "))
                  // TODO add a way to remove the now collected elements from the JobEventLogs
                  NoCache(Ok(Json.toJson(Json.obj("success" -> "new statistics added", "stat" -> statisticsObjectUpdated))))
                case None =>
                  Logger.info("Statistics generated, but it seems like the statistics could not be reloaded from the db")
                  NoCache(Ok(Json.toJson(Json.obj("error" -> "could not reload new stats from DB", "stat" -> statisticsObject))))
              }
            }
          } else {
            Logger.info("No need to push statistics. Last Push: " + statistics.lastPushed)
            Future.successful(NoCache(Ok(Json.toJson(Json.obj("success" -> "old statistics used", "stat" -> statistics)))))
          }
        }
      } else {
        Future.successful(NotFound)
      }
    }
  }

//  def statistics: Action[AnyContent] = Action.async { implicit request =>
//    userSessions.getUser.flatMap { user =>
//      if (user.isSuperuser) {
//        val dateTimeFirstOfMonth: DateTime = DateTime.now().dayOfMonth().withMinimumValue().withTimeAtStartOfDay()
//        val lastMonthQuery =
//          BSONDocument("$gte" -> BSONDateTime(dateTimeFirstOfMonth.minusMonths(1).getMillis),
//                       "$lt"  -> BSONDateTime(dateTimeFirstOfMonth.getMillis))
//        val currentMonthJobs = mongoStore
//          .findJobEventLogs(
//            BSONDocument(
//              JobEventLog.EVENTS ->
//              BSONDocument(
//                "$elemMatch" ->
//                BSONDocument(
//                  JobEvent.TIMESTAMP ->
//                  BSONDocument("$gte" -> BSONDateTime(dateTimeFirstOfMonth.getMillis))
//                )
//              )
//            )
//          )
//
//        val jobEventsSortedByTool = currentMonthJobs.map{ jobEventList =>
//          JobEventLog.toSortedMap(jobEventList)
//        }
//        jobEventsSortedByTool.map(_.map{ toolStatMap =>
//          val toolName = toolStatMap._1
//          val toolStat = toolStatMap._2
//          ToolStatistic(toolName,
//                        List(toolStat.length),
//                        List(toolStat.count(_.hasFailed)),
//                        List(toolStat.count(_.isDeleted)))
//        })
//        mongoStore.getStatistics.map { toolStatisticList: List[ToolStatistic] =>
//          NoCache(Ok(Json.toJson(toolStatisticList)))
//        }
//      } else {
//        Future.successful(NotFound)
//      }
//    }
//  }

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
