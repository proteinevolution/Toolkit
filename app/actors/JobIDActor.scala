package actors

import javax.inject.{ Inject, Singleton }

import actors.JobIDActor._
import akka.actor.{ ActorLogging, _ }
import akka.event.LoggingReceive
import de.proteinevolution.models.search.JobDAO
import de.proteinevolution.db.MongoStore
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.util.Random
import scala.concurrent.duration._

/**
  * Created by zin on 04.04.17.
  */
@Singleton
class JobIDActor @Inject()(mongoStore: MongoStore) extends Actor with ActorLogging {

  private val fetchLatestInterval = 5.seconds
  private val iter = Iterator
    .continually[String](Random.nextInt(9999999).toString.padTo(7, '0'))
    .filter(x => Await.result(isValid(x), scala.concurrent.duration.Duration.Inf))

  private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, Refill)(context.system.dispatcher)
  }

  // check ElasticSearch

  private def isValid(id: String): Future[Boolean] = {
    mongoStore.selectJob(id).map(_.isEmpty)
  }

  override def receive = LoggingReceive {

    case Refill =>
      if (jobIDRepo.length < 250) {
        jobIDRepo.enqueue(Seq.fill(200)(iter.next()): _*)
        jobIDRepo = jobIDRepo.distinct
        Logger.info("refilling jobID repository. Now " + jobIDRepo.length + " jobIDs in store.")
      }
  }

  override def preStart(): Unit = {
    jobIDRepo.enqueue(Seq.fill(50)(iter.next()): _*)
    jobIDRepo = jobIDRepo.distinct
  }

  override def postStop(): Unit = {

    Logger.info("JobIDActor crashed")
    Tick.cancel()
    self ! PoisonPill

  }

}

object JobIDActor {

  private var jobIDRepo: scala.collection.mutable.Queue[String] = scala.collection.mutable.Queue.empty[String]
  def provide: String = {
    jobIDRepo.dequeue()
  }

  case object Refill

}
