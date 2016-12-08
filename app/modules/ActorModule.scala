package modules

import actors.{JobActor, JobManager, UserManager, JobMonitor, Master}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorModule extends AbstractModule with AkkaGuiceSupport {


  def configure = {
    bindActor[JobManager]("jobManager") // Has information about the jobs
    bindActor[UserManager]("userManager") // Has information about the users
    bindActor[JobMonitor]("jobMonitor") // Real time monitoring
    bindActorFactory[JobActor, JobActor.Factory]
    bindActor[Master]("master")
  }
}
