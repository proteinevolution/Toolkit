package actors

import javax.inject.{ Inject, Singleton }

import actors.ClusterMonitor._
import actors.WebSocketActor.MaintenanceAlert
import akka.actor.{ ActorLogging, _ }
import akka.event.LoggingReceive
import controllers.Settings
import models.database.statistics.ClusterLoadEvent
import models.sge.Cluster
import modules.db.MongoStore
import modules.tel.TEL
import java.time.ZonedDateTime

import models.Constants
import de.proteinevolution.parsers.Ops.QStat
import play.api.Logger
import reactivemongo.bson.BSONObjectID
import services.JobActorAccess

import sys.process._
import scala.collection.immutable.HashSet
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by snam on 24.03.17.
  */
@Singleton
final class ClusterMonitor @Inject()(cluster: Cluster,
                                     mongoStore: MongoStore,
                                     jobActorAccess: JobActorAccess,
                                     val settings: Settings,
                                     constants: Constants)
    extends Actor
    with ActorLogging {

  case class RecordedTick(load: Double, timestamp: ZonedDateTime)

  private var record: List[Double]                = List.empty[Double]
  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]
  // Fetch the latest qhost status every 375ms
  private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, constants.pollingInterval, self, FetchLatest)(context.system.dispatcher)
  }
  private var nextStatisticsUpdateDate: ZonedDateTime = ZonedDateTime.now.plusMonths(1)

  override def preStart(): Unit = {
    if (settings.clusterMode == "LOCAL") context.stop(self)
  }

  override def postStop(): Unit = Tick.cancel()

  override def receive = LoggingReceive {

    case Connect(actorRef) =>
      watchers = watchers + actorRef

    case Disconnect(actorRef) =>
      watchers = watchers - actorRef

    case Multicast =>
      watchers.foreach { _ ! MaintenanceAlert }

    case FetchLatest =>
      //val load = cluster.getLoad.loadEst
      val qStat = QStat("qstat -xml".!!)

      // 32 Tasks are 100% - calculate the load from this.
      val load : Double = qStat.totalJobs().toDouble / constants.loadPercentageMarker

      jobActorAccess.broadcast(PolledJobs(qStat))
      Logger.info(s"[ClusterMonitor] Jobs currently listed in the cluster:\n${qStat.qStatJobs.map(_.sgeID).mkString(", ")}")

      /**
        * dynamically adjust the cluster resources dependent on the current cluster load
        */
      load match {
        // TODO check if there is a way to do this correctly (like setting a tool minimum and then adding cores / memory)
        // reducing the number of cores and memory is not a good idea! Some jobs need a minimum of these to run
        case x if x > 1.2            => TEL.memFactor = 1; TEL.threadsFactor = 1
        case x if x < 0.5 && x > 0.1 => TEL.memFactor = 1; TEL.threadsFactor = 1
        case x if x < 0.1            => TEL.memFactor = 1; TEL.threadsFactor = 1
        case _                       => TEL.memFactor = 1; TEL.threadsFactor = 1
      }
      // Update the record
      record = record.::(load)
      // send load message
      watchers.foreach(_ ! UpdateLoad(load))
      // if there are enough records, group them in and stick them in the DB collection
      if (record.length >= constants.loadRecordElements) self ! Recording
    
    /**
      * Writes the current load to the database and clears the record.
      */
    case Recording =>
      val loadAverage      = record.sum[Double] / record.length
      val currentTimestamp = ZonedDateTime.now
      mongoStore
        .upsertLoadStatistic(ClusterLoadEvent(BSONObjectID.generate(), record, loadAverage, Some(currentTimestamp)))
        .map { clusterLoadEvent =>
          //Logger.info("Average: " + loadAverage + " - " + record.mkString(", "))
          record = List.empty[Double]
        }
  }
}

object ClusterMonitor {

  case class Disconnect(actorRef: ActorRef)

  case class Connect(actorRef: ActorRef)

  case object FetchLatest

  case object Recording

  case object Multicast

  case class UpdateLoad(load: Double)

  case class ConnectedUsers(users: Int)

  case class PolledJobs(qStat : QStat)
}
