package controllers

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import akka.util.Timeout
import models.database.jobs.FrontendJob
import models.search.JobDAO
import modules.LocationProvider
import modules.db.MongoStore
import org.joda.time.DateTime
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future
import scala.concurrent.duration._

@Singleton
final class Tool @Inject()(val messagesApi: MessagesApi,
                           @NamedCache("userCache") implicit val userCache: CacheApi,
                           mongoStore : MongoStore,
                           implicit val mat: Materializer,
                           implicit val locationProvider: LocationProvider,
                           val jobDao: JobDAO)
    extends Controller
    with I18nSupport {

  implicit val timeout = Timeout(5.seconds)

  // counts usage of frontend tools in order to keep track for our stats

  def frontendCount(toolname: String): Action[AnyContent] = Action.async {

    // Add Frontend Job to Database
    mongoStore.addFrontendJob(
      FrontendJob(mainID = BSONObjectID.generate(),
                  parentID = None,
                  tool = toolname,
                  dateCreated = Some(DateTime.now()))
    )

    Future.successful(Ok)
  }

}
