package de.proteinevolution.cluster.actors

import akka.actor.{ Actor, ActorLogging, ActorRef, Cancellable }
import akka.event.LoggingReceive
import de.proteinevolution.cluster.actors.ClusterMonitor._
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.cluster.api.Polling.PolledJobs
import de.proteinevolution.cluster.api.QStat
import javax.inject.{ Inject, Singleton }

import scala.collection.immutable.HashSet
import scala.concurrent.duration._
import scala.sys.process._

@Singleton
final class ClusterMonitor @Inject()(
    constants: ConstantsV2
) extends Actor
    with ActorLogging {

  private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, constants.pollingInterval, self, FetchLatest)(
      context.system.dispatcher
    )
  }

  private def active(
      watchers: HashSet[ActorRef]
  ): Receive = {
    case Connect(actorRef) =>
      context.become(active(watchers + actorRef))
    case Disconnect(actorRef) =>
      context.become(active(watchers - actorRef))
    //case Multicast => watchers.foreach { _ ! MaintenanceAlert } TODO put somewhere else
    case FetchLatest =>
      val qStat = QStat("qstat -xml".!!)
      // 32 Tasks are 100% - calculate the load from this.
      val load: Double = qStat.totalJobs().toDouble / constants.loadPercentageMarker
      context.become(active(watchers))
      watchers.foreach(_ ! UpdateLoad(load))
      self ! PolledJobs(qStat)
  }

  override def preStart(): Unit = {
    // if (environment.mode == play.api.Mode.Dev && !TEL.isSubmitHost) context.stop(self)
  }

  override def postStop(): Unit = {
    val _ = Tick.cancel()
  }

  override def receive = LoggingReceive {
    active(HashSet.empty)
  }

}

object ClusterMonitor {
  case class Disconnect(actorRef: ActorRef)
  case class Connect(actorRef: ActorRef)
  case object FetchLatest
  case object Recording
  case object Multicast
  case class UpdateLoad(load: Double)
  case class ConnectedUsers(users: Int)
}
