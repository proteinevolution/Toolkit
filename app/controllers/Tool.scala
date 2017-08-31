package controllers

import java.time.ZonedDateTime
import javax.inject.{ Inject, Singleton }

import akka.stream.Materializer
import akka.util.Timeout
import models.database.jobs.FrontendJob
import models.search.JobDAO
import modules.LocationProvider
import modules.db.MongoStore
import play.api.cache._
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc._
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
final class Tool @Inject()(messagesApi: MessagesApi,
                           @NamedCache("userCache") implicit val userCache: SyncCacheApi,
                           mongoStore: MongoStore,
                           implicit val mat: Materializer,
                           implicit val locationProvider: LocationProvider,
                           val jobDao: JobDAO,
                           cc: ControllerComponents)
    extends AbstractController(cc)
    with I18nSupport {

  implicit val timeout: Timeout = Timeout(5.seconds)

  // counts usage of frontend tools in order to keep track for our stats

  def frontendCount(toolname: String): Action[AnyContent] = Action.async {

    // Add Frontend Job to Database
    mongoStore.addFrontendJob(
      FrontendJob(mainID = BSONObjectID.generate(),
                  parentID = None,
                  tool = toolname,
                  dateCreated = Some(ZonedDateTime.now))
    )

    Future.successful(Ok)
  }

}
