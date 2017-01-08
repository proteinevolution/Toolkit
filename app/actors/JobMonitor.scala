package actors


import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import models.database._

import scala.collection.immutable.HashSet
import akka.event.LoggingReceive

import scala.concurrent.duration._

/**
 * Real time logging of jobstate transitions
 * Created by snam on 11.11.16.
 */


class JobMonitor(jobID: String, status: JobState) extends Actor with ActorLogging {


  private val random = scala.util.Random

  private val fetchLatestInterval = 75.millis

  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]



  // Fetch the latest job value every 75ms
  val jobTick : Cancellable  = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, FetchLatest)(context.system.dispatcher)
  }


  def receive = LoggingReceive {

    case FetchLatest =>

    case WatchJob(_) =>

    case UnWatchJob(_) =>

  }

}


case object FetchLatest

case class WatchJob(job: String)

case class UnWatchJob(job: Option[String])