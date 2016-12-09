package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobActor
import actors.JobActor.RunscriptData
import actors.Master.CreateJob
import akka.actor.{ActorRef, ActorSystem, Props}
import models.job.JobIDProvider
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
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
                               @Named("master") master: ActorRef,
                               @NamedCache("jobActorCache") val jobActorCache: CacheApi,
                               val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with UserSessions {

  // Job Controller can directly send message to the JobActor if Possible, otherwise
  // the Master Actor needs to be used
  // TODO We might introduce more Masters and split them up on jobID ranges
  /**
    * Action requests a new Job instance at the Master.
    *
    */
  def create(toolname: String, jobIDoption : Option[String]):Action[AnyContent] = Action.async { implicit request =>



    // Determine whether the user Provided jobID is valid
    if (jobIDoption.isDefined && !jobIDProvider.isAvailable(jobIDoption.get)) {

      Future.successful(BadRequest)
    } else {

      val jobID = jobIDoption.getOrElse(jobIDProvider.provide)

      getUser.map { user =>

        master ! CreateJob(jobID, RunscriptData(toolname,
          request.body.asMultipartFormData.get.dataParts.mapValues(_.mkString)))
        Ok
      }
    }
  }

  /*
  context.actorOf(props(Props(create)), name)

   */

  def delete(jobID : String) = Action {

    Ok
  }




}
