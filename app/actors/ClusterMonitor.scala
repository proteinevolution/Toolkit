package actors

import javax.inject.{ Inject, Singleton }
import actors.ClusterMonitor._
import actors.WebSocketActor.MaintenanceAlert
import akka.actor.{ ActorLogging, _ }
import akka.event.LoggingReceive
import controllers.Settings
import de.proteinevolution.models.database.statistics.ClusterLoadEvent
import de.proteinevolution.db.MongoStore
import java.time.ZonedDateTime

import de.proteinevolution.models.Constants
import de.proteinevolution.parsers.Ops.QStat
import reactivemongo.bson.BSONObjectID
import services.JobActorAccess

import sys.process._
import scala.collection.immutable.HashSet
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
final class ClusterMonitor @Inject()(mongoStore: MongoStore,
                                     jobActorAccess: JobActorAccess,
                                     val settings: Settings,
                                     constants: Constants)(implicit ec: ExecutionContext)
    extends Actor
    with ActorLogging {

  def active(watchers: HashSet[ActorRef], record: List[Double]): Receive = {

    case Connect(actorRef) =>
      context become active(watchers + actorRef, record)

    case Disconnect(actorRef) =>
      context become active(watchers - actorRef, record)

    case Multicast => watchers.foreach { _ ! MaintenanceAlert }

    case FetchLatest =>
      //val load = cluster.getLoad.loadEst
      val qStat = QStat("qstat -xml".!!)

      // 32 Tasks are 100% - calculate the load from this.
      val load: Double = qStat.totalJobs().toDouble / constants.loadPercentageMarker

      jobActorAccess.broadcast(PolledJobs(qStat))

      // Update the record
      context become active(watchers, record.::(load))
      // send load message
      watchers.foreach(_ ! UpdateLoad(load))
      // if there are enough records, group them in and stick them in the DB collection
      if (record.length >= constants.loadRecordElements) self ! Recording

    case Recording =>
      val loadAverage      = record.sum[Double] / record.length
      val currentTimestamp = ZonedDateTime.now
      val _ = mongoStore
        .upsertLoadStatistic(ClusterLoadEvent(BSONObjectID.generate(), record, loadAverage, Some(currentTimestamp)))
        .map { _ =>
          context.become(active(watchers, Nil))
          ()
        }
  }

  private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, constants.pollingInterval, self, FetchLatest)(
      context.system.dispatcher
    )
  }

  override def preStart(): Unit = {
    if (settings.clusterMode == "LOCAL") context.stop(self)
  }

  override def postStop(): Unit = {
    val _ = Tick.cancel()
  }

  override def receive = LoggingReceive {
    active(HashSet.empty, List.empty[Double])
  }
}

object ClusterMonitor {
  case class RecordedTick(load: Double, timestamp: ZonedDateTime)
  case class Disconnect(actorRef: ActorRef)
  case class Connect(actorRef: ActorRef)
  case object FetchLatest
  case object Recording
  case object Multicast
  case class UpdateLoad(load: Double)
  case class ConnectedUsers(users: Int)
  case class PolledJobs(qStat: QStat)

}
