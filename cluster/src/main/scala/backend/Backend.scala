package backend

import akka.actor._
import akka.routing.RoundRobinPool
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Booting a cluster backend node with all actors
 */
object Backend extends App {

  // Simple cli parsing
  val port = args match {
    case Array()     => "0"
    case Array(port) => port
    case args        => throw new IllegalArgumentException(s"only ports. Args [ $args ] are invalid")
  }

  // System initialization
  val properties = Map(
      "akka.remote.netty.tcp.port" -> port
  )
  
  val system = ActorSystem("application", (ConfigFactory parseMap properties)
    .withFallback(ConfigFactory.load())
  )

  //system.actorOf(Props[WorkerActor].withRouter(RoundRobinPool(100)), name = "toolkitCluster")

  //Await.result(system.whenTerminated, Duration.Inf)
}
