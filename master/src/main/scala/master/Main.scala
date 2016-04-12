package master

import akka.actor.{ActorSystem, PoisonPill}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * This object is supposed to start the master node.
  *
  * Created by lzimmermann on 03.04.16.
  */
object Main extends App {



  val system = ActorSystem("ClusterSystem", ConfigFactory.load())

  system.actorOf(
      ClusterSingletonManager.props(
        Master.props,
        PoisonPill,
        ClusterSingletonManagerSettings(system).withRole("master")
      ), "master")

  Await.result(system.whenTerminated, Duration.Inf)
}