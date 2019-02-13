package de.proteinevolution.jobs.services

import akka.actor.{ ActorRef, ActorSystem, Props }
import de.proteinevolution.jobs.actors.JobActor
import de.proteinevolution.common.models.ConstantsV2
import javax.inject.{ Inject, Singleton }

@Singleton
final class JobActorAccess @Inject()(
    actorSystem: ActorSystem,
    jobActorFactory: JobActor.Factory,
    constants: ConstantsV2
) {

  // Just spawn all the JobActors
  private val jobActors: Seq[ActorRef] =
    Seq.tabulate(constants.nJobActors)(i => actorSystem.actorOf(Props(jobActorFactory.apply(i))))

  /**
   * Generates the corresponding hash value for a given jobID
   * @param jobID
   * @return
   */
  def jobIDHash(jobID: String): Int = {
    Math.abs(jobID.trim().hashCode()) % constants.nJobActors
  }

  /**
   * Sends a message to a specific JobActor
   * @param jobID
   * @param message
   */
  def sendToJobActor(jobID: String, message: Any): Unit = {
    this.jobActors(jobIDHash(jobID)) ! message
  }

  /**
   * Sends a message to all JobActors
   * @param message
   */
  def broadcast(message: Any): Unit = {
    this.jobActors.foreach(_ ! message)
  }
}
