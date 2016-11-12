package modules

import actors.{UserManager, JobManager}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport


class ActorModule extends AbstractModule with AkkaGuiceSupport {


  def configure = {
    bindActor[JobManager]("jobManager") // Has information about the jobs
    bindActor[UserManager]("userManager") // Has information about the users
  }
}