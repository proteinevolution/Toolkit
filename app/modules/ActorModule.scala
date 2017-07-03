package modules

import actors._
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[ClusterMonitor]("clusterMonitor")
    bindActor[FileWatcher]("fileWatcher")
    bindActor[JobIDActor]("jobIDActor")
    bindActor[DatabaseMonitor]("DatabaseMonitor")
    bindActorFactory[JobActor, JobActor.Factory]
    bindActorFactory[WebSocketActor, WebSocketActor.Factory]
  }
}
