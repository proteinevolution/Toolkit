package actors

import javax.inject.{Inject, Singleton}

import actors.ClusterMonitor.FetchLatest
import actors.FileWatcher.{StartProcessReport, StopProcessReport}
import akka.actor.{ActorLogging, _}
import akka.event.LoggingReceive
import models.database.jobs.Job
import models.database.users.User
import modules.db.MongoStore
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONDocument

import scala.collection.immutable.HashSet
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
    with MongoStore {


  private val fetchLatestInterval = 75.millis

  protected[this] var jobs : HashSet[String] = HashSet.empty[String]
  protected[this] var users : HashSet[String] = HashSet.empty[String]

  private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, FetchLatest)(context.system.dispatcher)
  }

  override def preStart(): Unit = {


  }

  override def postStop(): Unit = Tick.cancel()

  override def receive = LoggingReceive {

    case StartProcessReport(jobID: String) =>


      jobs = jobs + jobID


    case StopProcessReport(jobID: String) =>

      jobs = jobs - jobID


  }
}

object FileWatcher {

  case class StartProcessReport(jobID : String)
  case class StopProcessReport(jobID: String)

}
