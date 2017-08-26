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
import java.time.{ZonedDateTime, DateTimeFormatter}
import reactivemongo.bson.BSONObjectID

import sys.process._
import scala.collection.immutable.HashSet
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by snam on 24.03.17.
  */
@Singleton
final class ClusterMonitor @Inject()(cluster: Cluster, mongoStore: MongoStore, val settings: Settings)
    extends Actor
    with ActorLogging {

  case class RecordedTick(load: Double, timestamp: ZonedDateTime)
     
  private val qstatRegEx = "(\d+)\s+\S+\s+\S+\s+\S+\s+([a-z]+)\s+(\d\d\/\d\d\/\d\d\d\d \d\d:\d\d:\d\d)+\s+(\d+)\s*".r
  private val qstatDateTimePattern = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault())
     
  private val fetchLatestInterval                 = 3.seconds
  private val recordMaxLength                     = 20
  private var record: List[Double]                = List.empty[Double]
  protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]
  // Fetch the latest qhost status every 375ms
  private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, fetchLatestInterval, self, FetchLatest)(context.system.dispatcher)
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
      val qstatReply : String = ("qstat").!!
      val qstatParsed : List[QStatObject] = qstat.split("\n").drop(2).map(_ match {
          case qstatRegEx(clusterID, status, date, queueNumber) =>
            val dateFormatted = ZonedDateTime.parse(date, qstatDateTimePattern)
            status match {
              case "e" =>  Some(QStatObject(clusterID, failed = true,  finished = true,  dateFormatted, queueNumber.toInteger))
              case "x" =>  Some(QStatObject(clusterID, failed = false, finished = true,  dateFormatted, queueNumber.toInteger))
              case _ =>    Some(QStatObject(clusterID, failed = false, finished = false, dateFormatted, queueNumber.toInteger))
            }
          case _ =>
            None
        }).filterNot(_ == None).map(_.get)
   
      // 32 Tasks are 100% - calculate the load from this.
      val load : Double = qstatParsed.lenght.toDouble / 32

      /**
        * dynamically adjust the cluster resources dependent on the current cluster load
        */
      load match {
        //reducing the number of cores and memory is not a good idea! Some jobs need a minimum of these to run
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
      if (record.length >= recordMaxLength) self ! Recording
    
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

  case class QStatObject(clusterID : String, failed : Boolean, finished : Boolean, dateFormatted : ZonedDateTime, queueNumber : Int)
}
