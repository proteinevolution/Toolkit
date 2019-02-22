/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
