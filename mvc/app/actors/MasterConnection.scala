package actors

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import akka.cluster.singleton.{ClusterSingletonProxy, ClusterSingletonProxySettings}

/**
  *
  * This Actor Is the connection point between the Frontend Application and the Master Node
  * who accepts request from the Frontend.
  *
  *
  * Created by lzimmermann on 02.04.16.
  */
@Singleton
class MasterConnection @Inject() (system: ActorSystem) {



  val masterProxy = system.actorOf(
    ClusterSingletonProxy.props(
      settings = ClusterSingletonProxySettings(system).withRole("master"),
      singletonManagerPath = "/user/master"
    ),
    name = "masterProxy")
}
