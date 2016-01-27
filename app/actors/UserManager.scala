package actors

import actors.UserActor.AttachWS
import akka.actor._
import akka.event.LoggingReceive
import javax.inject._
import play.api.libs.concurrent.InjectedActorSupport
import play.api.cache._
import play.api.Logger

object UserManager {

  case class GetUserActor(key: String)
}


@Singleton
class UserManager @Inject() (childFactory: UserActor.Factory,
                             cache: CacheApi)
  extends Actor with ActorLogging with InjectedActorSupport {

  import actors.UserManager._

  val registeredUsers = scala.collection.mutable.Map[String, ActorRef]()


  def receive = LoggingReceive  {


    case GetUserActor(uid: String) =>

      val user = registeredUsers.getOrElseUpdate(uid, {

        injectedChild(childFactory(uid), uid)
      })
      sender() ! user

      // Assume cache miss
      cache.set(uid, user)
      context watch user


    case m @ AttachWS(uid, ws) =>

      Logger.info("[User Manager] Attach Web Socket")

      val user = registeredUsers.getOrElseUpdate(uid, {

        injectedChild(childFactory(uid), uid)
      })
      user ! m


    case Terminated(user) =>

      context stop user
  }
}
