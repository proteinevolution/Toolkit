package modules

import actors.{ESManager, UserManager, JobManager}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorModule extends AbstractModule with AkkaGuiceSupport {


  def configure = {
    bindActor[JobManager]("jobManager") // Has information about the jobs
    bindActor[UserManager]("userManager") // Has information about the users
    bindActor[ESManager]("esManager") // Has information about the Elastic search actor
  }
}