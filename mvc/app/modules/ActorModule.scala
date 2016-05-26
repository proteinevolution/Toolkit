package modules

import actors.JobManager
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorModule extends AbstractModule with AkkaGuiceSupport {


  def configure = {
    bindActor[JobManager]("jobManager") // Has information about the jobs
  }
}