import akka.actor.{ActorSystem, PoisonPill}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration

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