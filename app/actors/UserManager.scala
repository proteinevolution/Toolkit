package actors

import actors.UserActor.AttachWS
import akka.actor._
import akka.event.LoggingReceive
import javax.inject._
import play.api.libs.concurrent.InjectedActorSupport
import play.api.Logger
import play.api.cache._
object UserManager {

  case class GetUserActor(session_id: String)
}


@Singleton
class UserManager @Inject() (childFactory: UserActor.Factory)
  extends Actor with ActorLogging with InjectedActorSupport {

  import actors.UserManager._

  //
  val currentSessions = scala.collection.mutable.Map[String, ActorRef]()


  def receive = LoggingReceive  {


    case GetUserActor(session_id: String) =>

      val user = currentSessions.getOrElseUpdate(
        session_id,
        injectedChild(childFactory(session_id), session_id.toString)
      )
      sender() ! user

      context watch user

    case m @ AttachWS(session_id, ws) =>

      Logger.info("[User Manager] Attach Web Socket")

      val user = currentSessions.getOrElseUpdate(session_id, {

        injectedChild(childFactory(session_id), session_id.toString)
      })
      user ! m

    case Terminated(user) =>

      context stop user
  }
}
