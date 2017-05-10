package controllers

import javax.inject.{Inject, Singleton}

import models.database.jobs.Job
import models.database.statistics.{JobEvent, JobEventLog, ToolStatistic}
import models.database.users.User
import modules.LocationProvider
import org.joda.time.DateTime
import play.api.Logger
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by zin on 28.07.16.
  */
@Singleton
final class Backend @Inject()(webJarAssets: WebJarAssets,
                              settingsController: Settings,
                              @NamedCache("userCache") implicit val userCache: CacheApi,
                              implicit val locationProvider: LocationProvider,
                              val reactiveMongoApi: ReactiveMongoApi,
                              val messagesApi: MessagesApi)
    extends Controller
    with I18nSupport
    with Common
    with UserSessions {

  //TODO currently working mithril routes for the backend
  def index: Action[AnyContent] = Action.async { implicit request =>
    getUser.map { user =>
      if (user.isSuperuser) {
        NoCache(Ok(Json.toJson(List("Index Page"))))
      } else {
        NotFound
      }
    }
  }

  def statistics: Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      if (user.isSuperuser) {
        val dateTimeFirstOfMonth: DateTime = DateTime.now().dayOfMonth().withMinimumValue().withTimeAtStartOfDay()
        findJobEventLogs(
          BSONDocument(
            JobEventLog.EVENTS ->
              BSONDocument(
                "$elemMatch" ->
                  BSONDocument(JobEvent.TIMESTAMP ->
                    BSONDocument("$gte" -> BSONDateTime(dateTimeFirstOfMonth.minusMonths(1).getMillis),
                                 "$lt"  -> BSONDateTime(dateTimeFirstOfMonth.getMillis)))))).foreach { jobEventList =>
          Logger.info(
            "Found " + jobEventList.length + " Jobs for the last Month (From " + dateTimeFirstOfMonth
              .minusMonths(1) + " to " + dateTimeFirstOfMonth + ")")
        }

        getStatistics.map { toolStatisticList: List[ToolStatistic] =>
          NoCache(Ok(Json.toJson(toolStatisticList)))
        }
      } else {
        Future.successful(NotFound)
      }
    }
  }

  def pushMonthlyStatistics: Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      if (user.isSuperuser) {
        getStatistics
          .map { toolStatisticList: List[ToolStatistic] =>
            toolStatisticList.map { toolStatistic =>
              val updatedToolStatistic = toolStatistic.pushMonth()
              upsertStatistics(updatedToolStatistic)
              toolStatistic
            }
          }
          .map(toolStatistic => NoCache(Ok(Json.toJson(toolStatistic))))
      } else {
        Future.successful(NotFound)
      }
    }
  }

  def cms: Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      if (user.isSuperuser) {
        getArticles(-1).map { articles =>
          NoCache(Ok(Json.toJson(articles)))
        }
      } else {
        Future.successful(NotFound)
      }
    }
  }

  def users: Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      if (user.isSuperuser) {
        findUsers(BSONDocument(User.USERDATA -> BSONDocument("$exists" -> true))).map { users =>
          NoCache(Ok(Json.toJson(users)))
        }
      } else {
        Future.successful(NotFound)
      }
    }
  }
}
