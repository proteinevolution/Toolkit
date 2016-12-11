package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobActor
import actors.JobActor.RunscriptData
import actors.Master.CreateJob
import akka.actor.{ActorRef, ActorSystem}
import models.Values
import models.job.JobIDProvider
import models.search.JobDAO
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by lzimmermann on 02.12.16.
  */
@Singleton
class JobController @Inject() (jobIDProvider: JobIDProvider,
                               actorSystem : ActorSystem,
                               jobActorFactory : JobActor.Factory,
                               implicit val userCache : CacheApi,
                               final val values : Values,
                               @Named("master") master: ActorRef,
                               val jobDao           : JobDAO,
                               @NamedCache("jobitem") jobitemCache : CacheApi,
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
  def check(toolname: String, jobID: Option[String]) = Action.async {

    Logger.info("Reached JobController.check")

    // Determine the jobID
    (jobID match {

      case Some(id) =>
        Logger.info("Determine whether provided JobID is valid")
        selectJob(id).map { job => if (job.isDefined) Left(BadRequest) else Right(id) }
      case None =>

        Logger.info("Ask JobID Provider for new jobID")
        jobIDProvider.provide.map{s =>

          Logger.info("New jobID will be " + s)
          Right(s)}

    }).map {

      case Left(status) => status
      case Right(jobIDnew) =>


        // TODO Insert Code for Jobhashing and also include in the response
        Ok(Json.obj("jobID" -> jobIDnew))
    }
  }


  def create(toolname: String, jobID: String) =  Action.async { implicit request =>

    // Just grab the formData and send to Master
    getUser.map { user =>
      val formData = request.body.asMultipartFormData.get.dataParts.mapValues(_.mkString)
      master ! CreateJob(jobID, (user, None), RunscriptData(toolname, formData))
      Ok
    }
  }



  def delete(jobID : String ) = Action {



    Ok
  }

}
