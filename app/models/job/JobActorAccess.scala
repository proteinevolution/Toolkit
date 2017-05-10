package models.job

import javax.inject.{Inject, Singleton}
import actors.JobActor
import akka.actor.{ActorRef, ActorSystem, Props}
import models.Constants

/**
  * Created by lzimmermann on 29.01.17.
  */
@Singleton
final class JobActorAccess @Inject()(actorSystem: ActorSystem, jobActorFactory: JobActor.Factory) extends Constants {

  // Just spawn all the JobActors
  private val jobActors: Seq[ActorRef] = Seq.fill(nJobActors)(actorSystem.actorOf(Props(jobActorFactory.apply)))

  def sendToJobActor(jobID: String, message: Any): Unit = {
    this.jobActors(Math.abs(jobID.trim().hashCode()) % nJobActors) ! message
  }

  def broadcast(message: Any): Unit = {
    this.jobActors.foreach(_ ! message)
  }
}
