package actors

import akka.actor._
import akka.event.LoggingReceive
import javax.inject._
import play.api.libs.concurrent.InjectedActorSupport
object UserManager {

  case class GetUserActor(session_id: String)
}

/**
  * This actor manages the UserActors and knows, which UserActors are currently
  * present. The UserActor that belongs to a Session can always be extracted
  * from the UserManager.
  *
 */
@Singleton
class UserManager @Inject() (childFactory: UserActor.Factory)
  extends Actor with ActorLogging with InjectedActorSupport {

  import actors.UserManager._

  val currentSessions = scala.collection.mutable.Map[String, ActorRef]()

  def receive = LoggingReceive  {

    case GetUserActor(session_id: String) =>

      val user = currentSessions.getOrElseUpdate(
        session_id, {
          injectedChild(childFactory(session_id), session_id.toString)
        }
      )
      sender() ! user

      context watch user

    case Terminated(user) =>

      context stop user
  }
}
