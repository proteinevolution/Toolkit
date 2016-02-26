package actors

import actors.UserActor.AttachWS
import akka.actor._
import akka.event.LoggingReceive
import javax.inject._
import play.api.libs.concurrent.InjectedActorSupport
import play.api.Logger
import play.api.cache._
object UserManager {

  case class GetUserActor(user_id: Long)
}


@Singleton
class UserManager @Inject() (childFactory: UserActor.Factory)
  extends Actor with ActorLogging with InjectedActorSupport {

  import actors.UserManager._

  //
  val registeredUsers = scala.collection.mutable.Map[Long, ActorRef]()


  def receive = LoggingReceive  {


    case GetUserActor(user_id: Long) =>

      val user = registeredUsers.getOrElseUpdate(user_id, {

        injectedChild(childFactory(user_id), user_id.toString)
      })
      sender() ! user

      context watch user

    case m @ AttachWS(user_id, ws) =>

      Logger.info("[User Manager] Attach Web Socket")

      val user = registeredUsers.getOrElseUpdate(user_id, {

        injectedChild(childFactory(user_id), user_id.toString)
      })
      user ! m

    case Terminated(user) =>

      context stop user
  }
}
