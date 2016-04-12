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
// TODO Master Currently lives in this Actor System, Will be replaced by Akka Routing
@Singleton
class MasterConnection @Inject() (system: ActorSystem) {



  val masterProxy = system.actorOf(
    ClusterSingletonProxy.props(
      settings = ClusterSingletonProxySettings(system).withRole("master"),
      singletonManagerPath = "/user/master"
    ),
    name = "masterProxy")
}
