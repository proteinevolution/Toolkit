package actors


import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import akka.event.LoggingReceive
import models.sge.Cluster

import scala.collection.immutable.{HashSet, Queue}
import scala.concurrent.duration._


/**
  * Created by snam on 24.03.17.
  */


class ClusterMonitor @Inject()(cluster: Cluster) extends Actor with ActorLogging {

  private val random = scala.util.Random
  private val fetchLatestInterval = 375.millis

  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]


  // Fetch the latest qhost status every 375ms
  val Tick : Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, FetchLatest)(context.system.dispatcher)
  }

  override def receive = LoggingReceive {
    case FetchLatest =>
      val load = cluster.getLoad.loadEst
      println("current cluster load: " + load)

  }

}


case object FetchLatest