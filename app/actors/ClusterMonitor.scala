package actors


import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import akka.event.LoggingReceive
import models.sge.Cluster
import play.api.cache.{CacheApi, NamedCache}

import scala.collection.immutable.HashSet
import scala.concurrent.duration._


/**
  * Created by snam on 24.03.17.
  */

@Singleton
class ClusterMonitor @Inject()(cluster: Cluster) extends Actor with ActorLogging {

  private val fetchLatestInterval = 375.millis
  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]
  // Fetch the latest qhost status every 375ms
  val Tick : Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, FetchLatest)(context.system.dispatcher)
  }

  override def receive = LoggingReceive {

    case Connect =>
      watchers = watchers + sender

    case Disconnect =>
      watchers = watchers - sender

    case FetchLatest =>
      val load = cluster.getLoad.loadEst
      watchers.foreach(_ ! UpdateLoad(load: Double))

  }

}


case object Disconnect

case object Connect

case object FetchLatest

case class UpdateLoad(load : Double)
