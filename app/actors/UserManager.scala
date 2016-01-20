package actors

import akka.actor._
import akka.event.LoggingReceive
import play.api.Logger
import play.libs.Akka

class UserManager extends Actor with ActorLogging {


  val registeredUsers = new collection.mutable.HashMap[String, ActorRef]()


  def receive = LoggingReceive  {

    case SubscribeUser(uid) =>

      Logger.info("User session " + uid + " subscribed\n")

      registeredUsers.getOrElseUpdate(uid, {

          val newUser = Akka.system().actorOf(UserActor.props(uid))
          context watch newUser
          newUser
      })

    case TellUser(uid, message) =>

      registeredUsers.get(uid) match {

       case None =>  Logger.warn("You wanted to send a message to a user who is not subscribed")

       case Some(user) => user forward message
      }



    case Terminated(user) =>

        // TODO Remove terminated user from the HashMap of registered users
  }
}
/**
  * Created by lukas on 1/16/16.
  */
object UserManager  {


  lazy val theUserManager = Akka.system().actorOf(Props[UserManager])

  def apply() = theUserManager
}