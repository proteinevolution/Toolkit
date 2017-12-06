package controllers

import java.time.ZonedDateTime

import play.api.mvc._
import play.api.i18n.MessagesApi
import play.modules.reactivemongo.{ ReactiveMongoApi, ReactiveMongoComponents }
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
import javax.inject.{ Inject, Singleton }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }
import scala.sys.process._

/**
 *
 * this controller is used to modify system parameters and store them in the database
 *
 */
@Singleton
final class Settings @Inject()(messagesApi: MessagesApi,
                               val reactiveMongoApi: ReactiveMongoApi,
                               cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ReactiveMongoComponents {

  val clusterSettings: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection("settings").as[BSONCollection](FailoverStrategy()))
  val toolSettings: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection("toolSettings").as[BSONCollection](FailoverStrategy()))
  private[this] var cm = ""

  /**
   *
   * // sets if the toolkit is using the sge or executes jobs on localhost
   *
   * @param clusterMode
   */
  def setClusterMode(clusterMode: String) = Action {
    val document = BSONDocument("clusterMode" -> clusterMode,
                                "created_on" -> ZonedDateTime.now.toInstant.toEpochMilli,
                                "update_on"  -> ZonedDateTime.now.toInstant.toEpochMilli)
    val future = clusterSettings.flatMap(_.insert(document))
    future.onComplete {
      case Failure(e)         => throw e
      case Success(lastError) => println("successfully inserted document with lastError = " + lastError)
    }
    Ok("Got request")
  }

  def clusterMode: String = {
    val hostname_cmd = "hostname"
    val hostname     = hostname_cmd.!!.dropRight(1)
    if (hostname.equals("olt") || hostname.equals("rye"))
      cm = "sge"
    else
      cm = "LOCAL"
    cm
  }
}
