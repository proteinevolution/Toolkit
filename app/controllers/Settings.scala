package controllers

import java.util.Date
import javax.inject.Inject

import play.api.mvc.{Action, Controller}
import play.api.i18n.MessagesApi
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.BSONDocument

import scala.util.{Success, Failure}


/**
  * Created by zin on 28.07.16.
  *
  * this controller is used to modify system parameters and store them in the database
  *
  */

final class Settings @Inject() (val messagesApi: MessagesApi,
                           val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents {

  val settingsCollection: BSONCollection = db.collection("settings")
  val toolSettings: BSONCollection = db.collection("toolsettings")

  /**
    *
    * // sets if the toolkit is using the sge or executes jobs on localhost
    * @param clusterMode
    */

  def clusterMode(clusterMode : String) = {


    val document = BSONDocument(
      "clusterMode" -> clusterMode,
      "created_on" -> new Date(),
      "update_on" -> new Date())

    val future = settingsCollection.insert(document)


    future.onComplete {
      case Failure(e) => throw e
      case Success(lastError) => {
        println("successfully inserted document with lastError = " + lastError)
      }
    }

  }

  /**
    * sets h_vmem for a specific tool
    * @param memory
    */
  def memoryAllocation(memory : Int, toolName: String) = {


    val document = BSONDocument(
      "toolname" -> toolName,
      "memory" -> memory,
      "created_on" -> new Date(),
      "update_on" -> new Date())

    val future = toolSettings.insert(document)


    future.onComplete {
      case Failure(e) => throw e
      case Success(lastError) => {
        println("successfully inserted document with lastError = " + lastError)
      }
    }

  }

}