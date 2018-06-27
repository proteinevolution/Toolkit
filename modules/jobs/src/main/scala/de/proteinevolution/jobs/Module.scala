package de.proteinevolution.jobs

import com.google.inject.AbstractModule
import de.proteinevolution.jobs.actors.JobActor
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActorFactory[JobActor, JobActor.Factory]
  }

}
