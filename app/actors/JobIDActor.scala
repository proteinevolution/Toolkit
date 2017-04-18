package actors

import javax.inject.{Inject, Singleton}

import actors.JobIDActor._
import akka.actor._
import akka.event.LoggingReceive
import models.search.JobDAO
import modules.CommonModule
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi

import scala.collection.parallel.ParSeq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.Random
import scala.concurrent.duration._

/**
  * Created by zin on 04.04.17.
  */


@Singleton
class JobIDActor @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                           val jobDao : JobDAO) extends Actor with CommonModule {


  private val fetchLatestInterval = 5.seconds

  val Tick : Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, Ensure)(context.system.dispatcher)
  }


  // check ElasticSearch

  private def isValid(id : String) : Future[Boolean] = {


    jobDao.existsJobID(id).map {

      case x if x.totalHits == 0 => true
      case _ => false

    }

  }


  override def receive = LoggingReceive {


    case Ensure =>

      if(jobIDRepo.length < 100)
        self ! Refill


    case Refill =>

      val candIt = Iterator.continually[Seq[String]](Stream.continually(Random.nextInt(9999999).toString.padTo(7, '0')).take(100))
      jobIDRepo = jobIDRepo.union(candIt.next()).distinct.filter(x => Await.result(isValid(x), scala.concurrent.duration.Duration(1, "seconds")))

      Logger.info("refilling jobID repository. Now " + jobIDRepo.length + " jobIDs in store.")


  }


  override def preStart() : Unit = {

    val candIt = Iterator.continually[Seq[String]](Stream.continually(Random.nextInt(9999999).toString.padTo(7, '0')).take(50))
    jobIDRepo = jobIDRepo.union(candIt.next()).distinct.filter(x => Await.result(isValid(x), scala.concurrent.duration.Duration(1, "seconds")))

  }


  override def postStop() : Unit = {

    Logger.info("JobIDActor crashed")
    Tick.cancel()
    self ! PoisonPill

  }

}


object JobIDActor {


  private var jobIDRepo : ParSeq[String] = ParSeq.empty[String]


  def provide : String = {

    val pick = jobIDRepo.head
    jobIDRepo = jobIDRepo.zipWithIndex.collect {case (a,i) if a != pick => a}
    Logger.info(jobIDRepo.length + " jobIDs in in repository")
    pick

  }

  case object Refill
  case object Ensure

}
