package actors

import javax.inject.Singleton

import actors.JobIDActor._
import akka.actor.{Actor, ActorSystem}
import akka.stream.ActorMaterializer

/**
  * Created by zin on 04.04.17.
  */


@Singleton
class JobIDActor extends Actor {

  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()



  override def receive = {

    case GetId(id : String) =>

  }


}


object JobIDActor {

  case class GetId(id : String)
  case class GetIdReply(id : Option[String])

}
