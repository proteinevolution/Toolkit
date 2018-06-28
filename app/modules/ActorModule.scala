package modules

import actors._
import com.google.inject.AbstractModule
import de.proteinevolution.backend.actors.DatabaseMonitor
import de.proteinevolution.cluster.actors.ClusterMonitor
import de.proteinevolution.jobs.actors.JobActor
import play.api.libs.concurrent.AkkaGuiceSupport

class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[ClusterMonitor]("clusterMonitor")
    bindActor[DatabaseMonitor]("databaseMonitor")
    bindActorFactory[JobActor, JobActor.Factory]
    bindActorFactory[WebSocketActor, WebSocketActor.Factory]
  }

}
