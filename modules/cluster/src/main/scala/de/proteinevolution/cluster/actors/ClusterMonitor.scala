package de.proteinevolution.cluster.actors

import akka.NotUsed
import akka.actor.{ Actor, ActorLogging, ActorRef, Cancellable }
import akka.event.LoggingReceive
import de.proteinevolution.cluster.actors.ClusterMonitor._
import de.proteinevolution.cluster.models.QStat
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs.Job
import de.proteinevolution.tel.TEL
import javax.inject.{ Inject, Singleton }
import play.api.libs.ws.WSClient
import scala.collection.immutable.HashSet
import scala.concurrent.duration._
import scala.io.Source
import scala.sys.process._

@Singleton
final class ClusterMonitor @Inject()(
    constants: ConstantsV2,
    ws: WSClient
) extends Actor
    with ActorLogging {

  private def active(
      watchers: HashSet[ActorRef],
      currentJobs: Map[String, Job],
      toDelete: Map[String, Job]
  ): Receive = {
    case Connect(actorRef) =>
      context.become(active(watchers + actorRef, currentJobs, toDelete))
    case Disconnect(actorRef) =>
      context.become(active(watchers - actorRef, currentJobs, toDelete))
    //case Multicast => watchers.foreach { _ ! MaintenanceAlert } TODO put somewhere else
    case FetchLatest =>
      val qStat = QStat("qstat -xml".!!)
      // 32 Tasks are 100% - calculate the load from this.
      val load: Double = qStat.totalJobs().toDouble / constants.loadPercentageMarker
      context.become(active(watchers, currentJobs, toDelete))
      watchers.foreach(_ ! UpdateLoad(load))
      self ! PolledJobs(qStat)
    case RegisterJob(job: Job) =>
      context.become(active(watchers, currentJobs.updated(job.jobID, job), toDelete))
    case UnregisterJob(jobID: String) =>
      context.become(active(watchers, currentJobs.filter(_._1 != jobID), toDelete.filter(_._1 != jobID)))
    // Checks the current jobs against the currently running cluster jobs to see if there are any dead jobs
    case PolledJobs(qStat: QStat) =>
      val clusterJobIDs = qStat.qStatJobs.map(_.sgeID)
      currentJobs.values.foreach { job =>
        job.clusterData match {
          case Some(clusterData) =>
            val jobInCluster = clusterJobIDs.contains(clusterData.sgeID)
            if (currentJobs.isDefinedAt(job.jobID) && !jobInCluster && !toDelete.isDefinedAt(job.jobID)) {
              // mark dead jobs for deletion
              context.become(active(watchers, currentJobs, toDelete.updated(job.jobID, job)))
            } else if (currentJobs.isDefinedAt(job.jobID) && jobInCluster) {
              // save jobs which are alive again from deletion
              context.become(active(watchers, currentJobs, toDelete.filter(_._1 != job.jobID)))
            } else if (currentJobs.isDefinedAt(job.jobID) && !jobInCluster && toDelete.isDefinedAt(job.jobID)) {
              // kill jobs which are obviously dead
              val source = Source.fromFile(constants.jobPath + "/" + job.jobID + "/key")
              val key    = try { source.mkString.replaceAll("\n", "") } finally { source.close() }
              ws.url(s"https://${TEL.hostname}:${TEL.port}/jobs/error/${job.jobID}/$key").execute("PUT")
              context.become(
                active(watchers, currentJobs.filter(_._1 != job.jobID), toDelete.filter(_._1 != job.jobID))
              )
            }
          case None => NotUsed
        }
      }
  }

  private val Tick: Cancellable = {
    // scheduler should use the system dispatcher
    context.system.scheduler.schedule(Duration.Zero, constants.pollingInterval, self, FetchLatest)(
      context.system.dispatcher
    )
  }

  override def preStart(): Unit = {
    //if (environment.mode == play.api.Mode.Dev && !TEL.isSubmitHost) context.stop(self)
  }

  override def postStop(): Unit = {
    val _ = Tick.cancel()
  }

  override def receive = LoggingReceive {
    active(HashSet.empty, Map.empty[String, Job], Map.empty[String, Job])
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

  case class PolledJobs(qStat: QStat)

  case class RegisterJob(job: Job)

  case class UnregisterJob(jobID: String)

}
