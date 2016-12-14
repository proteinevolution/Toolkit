package modules

import actors.{JobActor,JobMonitor, Master}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorModule extends AbstractModule with AkkaGuiceSupport {


  def configure = {
    bindActor[JobMonitor]("jobMonitor") // Real time monitoring
    bindActorFactory[JobActor, JobActor.Factory]
    bindActor[Master]("master")
  }
}
