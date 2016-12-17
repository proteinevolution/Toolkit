package modules

import actors.{JobActor, JobMonitor, Master, WebSocketActor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorModule extends AbstractModule with AkkaGuiceSupport {


  def configure = {
    bindActor[JobMonitor]("jobMonitor") // Real time monitoring
    bindActor[Master]("master")

    bindActorFactory[JobActor, JobActor.Factory]
    bindActorFactory[WebSocketActor, WebSocketActor.Factory]
  }
}
