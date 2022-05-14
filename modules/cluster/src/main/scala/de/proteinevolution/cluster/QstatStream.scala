/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.{ ActorAttributes, Materializer, Supervision }
import de.proteinevolution.cluster.api.Polling.PolledJobs
import de.proteinevolution.cluster.api.QStat
import de.proteinevolution.common.models.ConstantsV2
import javax.inject.{ Inject, Singleton }
import play.api.Logging

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.sys.process._

@Singleton
final private[cluster] class QstatStream @Inject(constants: ConstantsV2)(implicit
    ec: ExecutionContext,
    mat: Materializer,
    system: ActorSystem
) extends Logging {

  private[this] def qStat(): Unit = {
    val qStat = QStat("qstat -xml".!!)
    system.eventStream.publish(PolledJobs(qStat))
  }

  Source
    .tick(5.seconds, constants.pollingInterval, NotUsed)
    .withAttributes(ActorAttributes.supervisionStrategy { t =>
      logger.error("qstat tick stream crashed", t)
      Supervision.Resume
    })
    .runForeach(_ => qStat())

}
