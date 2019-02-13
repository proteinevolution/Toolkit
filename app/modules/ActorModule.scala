package modules

import com.google.inject.AbstractModule
import de.proteinevolution.backend.actors.DatabaseMonitor
import de.proteinevolution.jobs.actors.JobActor
import de.proteinevolution.message.actors.WebSocketActor
import play.api.libs.concurrent.AkkaGuiceSupport

final class ActorModule extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[DatabaseMonitor]("databaseMonitor")
    bindActorFactory[JobActor, JobActor.Factory]
    bindActorFactory[WebSocketActor, WebSocketActor.Factory]
  }

}
