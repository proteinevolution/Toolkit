package modules

import actors.{ ClusterMonitor, JobActor, JobIDActor, WebSocketActor }
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[ClusterMonitor]("clusterMonitor")
    bindActor[JobIDActor]("jobIDActor")
    bindActorFactory[JobActor, JobActor.Factory]
    bindActorFactory[WebSocketActor, WebSocketActor.Factory]
  }
}
