package actors

import javax.inject.{Inject, Singleton}

import actors.ClusterMonitor.FetchLatest
import actors.FileWatcher.WatchProcessFile
import akka.actor.{ActorLogging, _}
import akka.event.LoggingReceive
import models.Constants
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import akka.actor.{ActorRef, ActorSystem}
import better.files._, FileWatcher._

/**
  * Created by snam on 13.06.17.
  * Reports process log updates to the frontend
  */
@Singleton
final class FileWatcher @Inject()(val reactiveMongoApi: ReactiveMongoApi)
  extends Actor
    with ActorLogging {



  protected[this] var jobLogs : scala.collection.immutable.Map[String, ActorRef] = Map.empty[String, ActorRef]


  override def preStart(): Unit = {}

  override def postStop(): Unit = {}

  override def receive = LoggingReceive {

    case WatchProcessFile(jobID: String, wsActor : ActorRef) =>

      jobLogs = jobLogs + (jobID -> wsActor)


  }
}

object FileWatcher {

  case class WatchProcessFile(jobID : String, wsActor : ActorRef)

}
