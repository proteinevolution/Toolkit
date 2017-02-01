package models.job

import javax.inject.{Inject, Singleton}

import actors.JobActor
import akka.actor.{ActorRef, ActorSystem, Props}
import models.Constants
import reactivemongo.bson.BSONObjectID

/**
  * Created by lzimmermann on 29.01.17.
  */

@Singleton
class JobActorAccess @Inject() (actorSystem: ActorSystem,
                                 jobActorFactory: JobActor.Factory) extends Constants {

  def sendToJobActor(jobID: String, message: Any): Unit = {
    this.jobActors(Math.abs(jobID.trim().hashCode()) % nJobActors) ! message
  }

  def identifyUser(message: Any) : Unit = {
    this.jobActors.foreach(_ ! message)
  }

  // Just spawn all the JobActors
  private val jobActors: Seq[ActorRef] = Seq.fill(nJobActors)(actorSystem.actorOf(Props(jobActorFactory.apply)))
}
