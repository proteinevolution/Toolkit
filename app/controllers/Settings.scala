package controllers


import java.util.Date
import play.api.mvc.{Action, Controller}
import play.api.i18n.MessagesApi
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.sys.process._

/**
  * Created by zin on 28.07.16.
  *
  * this controller is used to modify system parameters and store them in the database
  *
  */


@Singleton
final class Settings @Inject() (val messagesApi       : MessagesApi,
                                val reactiveMongoApi  : ReactiveMongoApi)
                                extends Controller with MongoController with ReactiveMongoComponents {





  val clusterSettings : Future[BSONCollection] = reactiveMongoApi.database.map(_.collection("settings").as[BSONCollection](FailoverStrategy()))
  val toolSettings : Future[BSONCollection] = reactiveMongoApi.database.map(_.collection("toolSettings").as[BSONCollection](FailoverStrategy()))
  private[this] var cm = ""

  /**
    *
    * // sets if the toolkit is using the sge or executes jobs on localhost
    *
    * @param clusterMode
    */

  def setClusterMode(clusterMode : String) = Action {


    val document = BSONDocument(
      "clusterMode" -> clusterMode,
      "created_on" -> new Date(),
      "update_on" -> new Date())

    val future = clusterSettings.flatMap(_.insert(document))

    future.onComplete {
      case Failure(e) => throw e
      case Success(lastError) => println("successfully inserted document with lastError = " + lastError)
    }

    Ok("Got request")

  }


  def clusterMode : String = {

    val hostname_cmd = "hostname"
    val hostname = hostname_cmd.!!.dropRight(1)

    if (hostname.equals("olt"))

      cm = "sge"

    else

      cm = "LOCAL"

    cm
  }


  /**
    * sets h_vmem for a specific tool
    *
    * @param memory
    */
  def setMemoryAllocation(memory : Int, toolName: String) = Action {


    val document = BSONDocument(
      "toolname" -> toolName,
      "memory" -> memory,
      "created_on" -> new Date(),
      "update_on" -> new Date())

    val future = toolSettings.flatMap(_.insert(document))


    future.onComplete {
      case Failure(e) => throw e
      case Success(lastError) => {
        println("successfully inserted document with lastError = " + lastError)
      }
    }

    Ok("Got request")

  }

  def getMemoryAlloc(toolName : String) = "42" // IMPLEMENT ME
}
