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

package de.proteinevolution.jobs.services

import akka.actor.{ ActorRef, ActorSystem, Props }
import de.proteinevolution.jobs.actors.JobActor
import de.proteinevolution.common.models.ConstantsV2
import javax.inject.{ Inject, Singleton }

@Singleton
final class JobActorAccess @Inject() (
    actorSystem: ActorSystem,
    jobActorFactory: JobActor.Factory,
    constants: ConstantsV2
) {

  // Just spawn all the JobActors
  private val jobActors: Seq[ActorRef] =
    Seq.tabulate(constants.nJobActors)(i => actorSystem.actorOf(Props(jobActorFactory.apply(i))))

  /**
   * Generates the corresponding hash value for a given jobID
   * @param jobID
   * @return
   */
  def jobIDHash(jobID: String): Int = {
    Math.abs(jobID.trim().hashCode()) % constants.nJobActors
  }

  /**
   * Sends a message to a specific JobActor
   * @param jobID
   * @param message
   */
  def sendToJobActor(jobID: String, message: Any): Unit = {
    this.jobActors(jobIDHash(jobID)) ! message
  }

}
