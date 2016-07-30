package akkaguice

import akka.actor.ActorSystem
import akkaguice.AkkaModule.ActorSystemProvider
import com.google.inject.{AbstractModule, Injector, Provider}
import com.typesafe.config.Config
import javax.inject.Inject

import net.codingwell.scalaguice.ScalaModule

object AkkaModule {

  class ActorSystemProvider @Inject() (val config: Config, val injector: Injector) extends Provider[ActorSystem] {
    override def get() = {

      val system = ActorSystem("ClusterSystem", config)
      // add the GuiceAkkaExtension to the system, and initialize it with the Guice injector
      GuiceAkkaExtension(system).initialize(injector)
      system
    }
  }
}

/**
  * A module providing an Akka ActorSystem.
  */
class AkkaModule extends AbstractModule with ScalaModule {


  override def configure {
    bind[ActorSystem].toProvider[ActorSystemProvider].asEagerSingleton()
  }
}