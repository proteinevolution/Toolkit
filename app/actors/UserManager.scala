package actors

import akka.actor.{Props, Actor, ActorLogging}
import akka.event.LoggingReceive
import play.libs.Akka


// TODO There is currently only one userManager, might become a bottleneck

/**
  * Created by lukas on 1/16/16.
  */
class UserManager extends Actor with ActorLogging {



  def receive = LoggingReceive  {


    null


  }
}
object UserManager {

  lazy val theUserManager = Akka.system().actorOf(Props[UserManager])

  def apply() = theUserManager
}
