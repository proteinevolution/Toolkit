package actors

import akka.actor.{ActorSystem, Props}

/**
  * Created by lzimmermann on 02.04.16.
  */
// TODO Master Currently lives in this Actor System, Will be replaced by Akka Routing
object MasterConnection {

  private val system = ActorSystem("masterSystem")
  val master = system.actorOf(Props[Master], "master")
}
