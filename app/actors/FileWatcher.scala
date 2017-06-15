package actors

import javax.inject.{Inject, Singleton}

import actors.ClusterMonitor.FetchLatest
import actors.FileWatcher.{StartFileWatching, StopFileWatching}
import akka.actor.{ActorLogging, _}
import akka.event.LoggingReceive
import models.Constants
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
  * Created by snam on 13.06.17.
  * Reports process log updates to the frontend
  */
@Singleton
final class FileWatcher @Inject()(val reactiveMongoApi: ReactiveMongoApi)
  extends Actor
    with ActorLogging
    with Constants {



  private[this] val fetchLatestInterval = 75.millis

  protected[this] var jobLogs : scala.collection.immutable.Map[String, ActorRef] = Map.empty[String, ActorRef]


  private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, FetchLatest)(context.system.dispatcher)
  }

  override def preStart(): Unit = {}

  override def postStop(): Unit = Tick.cancel()

  override def receive = LoggingReceive {

    case StartFileWatching(jobID: String, wsActor : ActorRef) =>

      jobLogs = jobLogs + (jobID -> wsActor)


    case StopFileWatching(jobID: String) =>

      jobLogs -= jobID


    case FetchLatest =>

      jobLogs.foreach {
        println
      }


  }
}

object FileWatcher {


  case class StartFileWatching(jobID : String, wsActor : ActorRef)
  case class StopFileWatching(jobID: String)


}
