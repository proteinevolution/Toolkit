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

package modules

import com.google.inject.AbstractModule
import de.proteinevolution.backend.actors.DatabaseMonitor
import de.proteinevolution.cluster.ClusterSubscriber
import de.proteinevolution.jobs.actors.JobActor
import de.proteinevolution.message.actors.WebSocketActor
import play.api.libs.concurrent.AkkaGuiceSupport

final class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[DatabaseMonitor]("databaseMonitor")
    bindActorFactory[JobActor, JobActor.Factory]
    bindActorFactory[WebSocketActor, WebSocketActor.Factory]
    bindActor[ClusterSubscriber]("clusterSubscriber")
  }

}
