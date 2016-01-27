package modules

import akka.routing.RoundRobinPool
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import actors.{UserActor, UserManager, Worker}


/**
  * Created by lukas on 1/26/16.
  */
class ActorBinding extends AbstractModule with AkkaGuiceSupport {


  def configure = {

    bindActor[UserManager]("user-manager")
    bindActorFactory[UserActor, UserActor.Factory]
    bindActor[Worker]("worker", RoundRobinPool(4).props)
  }
}
