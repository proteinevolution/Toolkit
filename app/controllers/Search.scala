package controllers

import actors.ESManager.Search
import akka.actor.ActorRef
import models.Constants
import play.api.cache._
import play.api.libs.json.Json
import javax.inject.{Named, Singleton, Inject}
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import models.search.JobDAO
import play.api.mvc.{Action, Controller}
import org.joda.time.DateTime

import scala.concurrent.Future


@Singleton
final class Search @Inject() (
          @NamedCache("userCache") userCache        : CacheApi,
                               val reactiveMongoApi : ReactiveMongoApi,
               @Named("esManager") esManager        : ActorRef)
                           extends Controller with Constants
                                              with ReactiveMongoComponents
                                              with UserSessions {

  // TODO more actions from the views

  def getJob() = Action.async { implicit request =>
    val userCollection = reactiveMongoApi.database.map(_.collection("jobs").as[BSONCollection](FailoverStrategy()))
    // Retrieve the user from the cache or the DB
    getUser(request, userCollection, userCache).flatMap { user =>
      esManager ! Search(user.userID, "", -1)
      Future.successful(Ok)
    }
  }
}
