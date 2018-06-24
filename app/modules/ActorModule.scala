package modules

import actors._
import com.google.inject.AbstractModule
import de.proteinevolution.cluster.actors.ClusterMonitor
import play.api.libs.concurrent.AkkaGuiceSupport

class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[ClusterMonitor]("clusterMonitor")
    bindActor[DatabaseMonitor]("DatabaseMonitor")
    bindActorFactory[JobActor, JobActor.Factory]
    bindActorFactory[WebSocketActor, WebSocketActor.Factory]
  }

}
