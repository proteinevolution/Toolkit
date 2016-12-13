package controllers

import akka.actor.ActorRef
import javax.inject.{Inject, Named, Singleton}

import actors.JobActor.JobStateChanged
import actors.JobManager.{AddSGEjobID, UpdateDateViewed}
import actors.Master.JobMessage
import actors.UserManager.RunningJobMessage
import models.database._
import modules.CommonModule
import play.api.cache.{CacheApi, NamedCache}
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONDocument

/*
TODO
We can introduce auto-coercion of the Job MainID to the BSONObject ID
 */
/**
  * This controller is supposed to handle request coming from the Backend, such as compute
  * nodes from a gridengine.
  *
  */
@Singleton
final class Jobs @Inject()(@Named("jobManager") jobManager : ActorRef,
                           @Named("userManager") userManager : ActorRef,
                           @Named("master") master: ActorRef,
                           val reactiveMongoApi: ReactiveMongoApi,
                           @NamedCache("jobActorCache") val jobActorCache: CacheApi) extends Controller with CommonModule {

  def jobStatusDone(jobID: String) = Action {

    master ! JobMessage(jobID, JobStateChanged(jobID, Done))
    Ok
  }

  def jobStatusError(jobID: String) = Action {
    master ! JobMessage(jobID, JobStateChanged(jobID, Error))
    Ok
  }

  def jobStatusRunning(jobID: String) = Action {
    master ! JobMessage(jobID, JobStateChanged(jobID, Running))
    Ok
  }

  def jobStatusQueued(jobID: String) = Action {
    master ! JobMessage(jobID, JobStateChanged(jobID, Queued))
    Ok
  }

  def SGEID(jobID: String, sgeID: String) = Action {
    jobManager ! AddSGEjobID(reactivemongo.bson.BSONObjectID.parse(jobID).get, sgeID)
    Ok

  }

  def pushMessage(jobID : String, message : String)  = Action {
    userManager ! RunningJobMessage(reactivemongo.bson.BSONObjectID.parse(jobID).get, message)
    Ok
  }

  def updateDateViewed(jobID : String)  = Action {
    jobManager ! UpdateDateViewed(reactivemongo.bson.BSONObjectID.parse(jobID).get)
    Ok
  }
}