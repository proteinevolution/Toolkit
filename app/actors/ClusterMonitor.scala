package actors


import javax.inject.{Inject, Singleton}

import actors.ClusterMonitor._
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import akka.event.LoggingReceive
import models.sge.Cluster
import org.joda.time.DateTime
import play.api.Logger

import scala.collection.immutable.HashSet
import scala.concurrent.duration._


/**
  * Created by snam on 24.03.17.
  */

@Singleton
final class ClusterMonitor @Inject()(cluster: Cluster) extends Actor with ActorLogging {


  case class RecordedTick(load: Double, timestamp : DateTime)

  private val fetchLatestInterval = 3000.millis
  private val recordingInterval = 10.minutes
  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]
  // Fetch the latest qhost status every 375ms
  val Tick : Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, FetchLatest)(context.system.dispatcher)
  }


  override def receive = LoggingReceive {

    case Connect(actorRef) =>
      watchers = watchers + actorRef

    case Disconnect(actorRef) =>
      watchers = watchers - actorRef

    case FetchLatest =>
      val load = cluster.getLoad.loadEst
      //val messagingTime = DateTime.now()
      watchers.foreach(_ ! UpdateLoad(load))
      //Logger.info( s"""Updated Load with ${watchers.size} Users. Time needed: ${DateTime.now().getMillis - messagingTime.getMillis}ms""".stripMargin)
      //watchers.foreach(_ ! ConnectedUsers(watchers.size))
      //println(load)

    case Recording =>
      val load = cluster.getLoad.loadEst
      val currentTimestamp = DateTime.now()


  }

}

object ClusterMonitor {

  case class Disconnect(actorRef : ActorRef)

  case class Connect(actorRef : ActorRef)

  case object FetchLatest

  case object Recording

  case class UpdateLoad(load: Double)

  case class ConnectedUsers(users: Int)

}