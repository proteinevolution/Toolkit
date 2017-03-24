package actors


import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import akka.event.LoggingReceive

import scala.collection.immutable.{HashSet, Queue}
import scala.concurrent.duration._


/**
  * Created by snam on 24.03.17.
  */


class ClusterMonitor extends Actor with ActorLogging {

  private val random = scala.util.Random
  private val fetchLatestInterval = 75.millis

  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]


  // Fetch the latest qhost status every 75ms
  val Tick : Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, FetchLatest)(context.system.dispatcher)
  }

  override def receive = LoggingReceive {
    case FetchLatest =>
    //do polling work here
  }

}


case object FetchLatest