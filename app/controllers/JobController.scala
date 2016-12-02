package controllers

import javax.inject.{Inject, Singleton}

import actors.JobActor
import actors.JobActor.Delete
import akka.actor.{ActorSystem, Props}
import models.job.JobIDProvider
import play.api.Logger
import play.api.cache.CacheApi
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by lzimmermann on 02.12.16.
  */
@Singleton
class JobController @Inject() (jobIDProvider: JobIDProvider,
                               actorSystem : ActorSystem,
                               jobActorFactory : JobActor.Factory,
                               implicit val userCache : CacheApi,
                               val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with UserSessions {

  /**
    * Creates a new Job by instanciating a corresponding job actor
    *
    */
  def create(jobIDoption : Option[String]):Action[AnyContent] = Action.async { implicit request =>

    if(jobIDoption.isDefined && !jobIDProvider.isAvailable(jobIDoption.get)) {

      Future.successful(BadRequest)
    }
    val jobID = jobIDoption.getOrElse(jobIDProvider.provide)

    getUser.map { user =>

          Logger.info(s"JobID: $jobID -- Spawn JobActor")
          val jobActor = actorSystem.actorOf(Props(jobActorFactory(jobID, user.userID.stringify)), jobID)
          Ok
        }
    }

  /*
  context.actorOf(props(Props(create)), name)

   */

  def delete(jobID : String) = Action {

    actorSystem.actorSelection(s"user/$jobID") ! Delete
    Ok
  }
}
