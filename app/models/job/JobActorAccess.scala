package models.job

import javax.inject.{ Inject, Singleton }
import actors.JobActor
import akka.actor.{ ActorRef, ActorSystem, Props }
import models.Constants

/**
  * Created by lzimmermann on 29.01.17.
  */
@Singleton
final class JobActorAccess @Inject()(actorSystem: ActorSystem, jobActorFactory: JobActor.Factory, constants: Constants) {

  // Just spawn all the JobActors
  private val jobActors: Seq[ActorRef] =
    Seq.tabulate(constants.nJobActors)(i => actorSystem.actorOf(Props(jobActorFactory.apply(i))))

  def sendToJobActor(jobID: String, message: Any): Unit = {
    this.jobActors(Math.abs(jobID.trim().hashCode()) % constants.nJobActors) ! message
  }

  def broadcast(message: Any): Unit = {
    this.jobActors.foreach(_ ! message)
  }
}
