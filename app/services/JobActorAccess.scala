package services

import javax.inject.{ Inject, Singleton }

import actors.JobActor
import akka.actor.{ ActorRef, ActorSystem, Props }
import de.proteinevolution.models.ConstantsV2

@Singleton
final class JobActorAccess @Inject()(actorSystem: ActorSystem,
                                     jobActorFactory: JobActor.Factory,
                                     constants: ConstantsV2) {

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
