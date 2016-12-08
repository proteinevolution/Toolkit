package actors



import javax.inject.{Inject, Singleton}

import akka.actor.Actor
import akka.event.LoggingReceive
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}

/**
  * Created by lzimmermann on 08.12.16.
  */
@Singleton
class Master @Inject() (@NamedCache("jobActorCache") val jobActorCache: CacheApi) extends Actor {




  override def preStart(): Unit = {

    Logger.info("Master is available")
  }


  def receive = LoggingReceive {


    case  _ =>

  }
}
