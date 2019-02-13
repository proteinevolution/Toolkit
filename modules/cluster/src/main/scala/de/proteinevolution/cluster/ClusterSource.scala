package de.proteinevolution.cluster

import akka.NotUsed
import akka.actor.{ ActorSystem, Cancellable }
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import de.proteinevolution.cluster.ClusterSource.UpdateLoad
import de.proteinevolution.cluster.api.Polling.PolledJobs
import de.proteinevolution.cluster.api.QStat
import de.proteinevolution.common.models.ConstantsV2
import javax.inject.{ Inject, Singleton }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.sys.process._

@Singleton
final class ClusterSource @Inject()(constants: ConstantsV2)(
    implicit system: ActorSystem,
    mat: Materializer,
    ec: ExecutionContext
) {

  private[this] def update(): Unit = {
    val qStat = QStat("qstat -xml".!!)
    // 32 Tasks are 100% - calculate the load from this.
    val load: Double = qStat.totalJobs().toDouble / constants.loadPercentageMarker
    system.eventStream.publish(PolledJobs(qStat))
    system.eventStream.publish(UpdateLoad(load))
  }

  private[this] val tick: Source[NotUsed, Cancellable] = Source.tick(0.seconds, constants.pollingInterval, NotUsed)

  tick.runForeach(_ => update())

}

object ClusterSource {
  case class UpdateLoad(load: Double)
}
