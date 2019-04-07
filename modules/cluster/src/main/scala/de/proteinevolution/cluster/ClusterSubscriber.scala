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

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{ Actor, ActorLogging, OneForOneStrategy, SupervisorStrategy }
import akka.event.LoggingReceive
import akka.stream.Materializer
import de.proteinevolution.cluster.ClusterSubscriber.UpdateLoad
import de.proteinevolution.cluster.api.SGELoad
import de.proteinevolution.cluster.api.SGELoad.UpdateRunningJobs
import de.proteinevolution.common.models.ConstantsV2
import javax.inject.{ Inject, Singleton }

import scala.concurrent.ExecutionContext

@Singleton
final class ClusterSubscriber @Inject()(constants: ConstantsV2)(
    implicit ec: ExecutionContext,
    mat: Materializer
) extends Actor
    with ActorLogging {

  private[this] def calculated(runningJobs: Int): Double = runningJobs.toDouble / constants.loadPercentageMarker

  private[this] def active(runningJobs: Int): Receive = {

    case SGELoad.Ask => sender ! UpdateLoad(calculated(runningJobs))

    case UpdateRunningJobs(t) =>
      t match {
        case SGELoad.+ =>
          val newJobCount = runningJobs + 1
          if (newJobCount > constants.loadPercentageMarker) log.info(s"cluster load critical: > $newJobCount jobs")
          context.system.eventStream.publish(UpdateLoad(calculated(newJobCount)))
          context.become(active(newJobCount))
        case SGELoad.- =>
          val newJobCount = if (runningJobs > 0) runningJobs - 1 else runningJobs
          context.system.eventStream.publish(UpdateLoad(calculated(newJobCount)))
          context.become(active(newJobCount))
      }
  }

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[UpdateRunningJobs]).asInstanceOf[Unit]
  }

  override def postStop(): Unit = {
    context.system.eventStream.unsubscribe(self, classOf[UpdateRunningJobs]).asInstanceOf[Unit]
  }

  override def supervisorStrategy: SupervisorStrategy = {
    OneForOneStrategy() {
      case t =>
        log.error(s"clusterSubscriber crashed", t)
        Restart
    }
  }

  override def receive: Receive = LoggingReceive {
    active(0)
  }

}

object ClusterSubscriber {
  case class UpdateLoad(load: Double)
}
