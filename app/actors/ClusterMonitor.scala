package actors


import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable, Props}
import akka.event.LoggingReceive
import models.job.JobActorAccess
import models.sge.Cluster
import play.api.cache.{CacheApi, NamedCache}
import play.libs.Akka

import scala.collection.immutable.HashSet
import scala.concurrent.duration._


/**
  * Created by snam on 24.03.17.
  */

@Singleton
class ClusterMonitor @Inject()(cluster: Cluster,
                               jobActorAccess  : JobActorAccess,
                               @NamedCache("userCache") implicit val userCache : CacheApi) extends Actor with ActorLogging {

  private val random = scala.util.Random
  private val fetchLatestInterval = 375.millis

  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]


  // Fetch the latest qhost status every 375ms
  val Tick : Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, FetchLatest)(context.system.dispatcher)
  }

  //val websocket : ActorRef = Akka.system.actorOf(Props[WebSocketActor])

  override def receive = LoggingReceive {
    case FetchLatest =>
      val load = cluster.getLoad.loadEst
      println("current cluster load: " + load)
      jobActorAccess.broadcast(UpdateLoad(load))

  }

}


case object FetchLatest

case class UpdateLoad(load : Double)