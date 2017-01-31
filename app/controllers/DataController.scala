package controllers

import javax.inject.Inject

import modules.CommonModule
import play.api.Logger
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by lzimmermann on 26.01.17.
  */
class DataController  @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with CommonModule {

  /** Check whether the user is allowed to fetch the data for the particular job and retrieves the data with
    * stored given a particular key
   */
  def get(jobID: String, key: String) = Action.async {
    getResult(jobID, key).map {
      case Some(jsValue) => Ok(jsValue)
      case None => NotFound
    }
  }
}
