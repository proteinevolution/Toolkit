package actors

import akka.actor.{Actor, RootActorPath}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{Cluster, MemberStatus}
import akka.event.LoggingReceive



/**
  *
  *
  *
  * Created by lzimmermann on 28.04.16.
  */
class JobDatabaseActor extends Actor {

  val cluster = Cluster.get(context.system)


  override def preStart = {

    cluster.subscribe(self, classOf[MemberUp])
  }

  override def postStop = {

    cluster.unsubscribe(self)
  }


  def receive =  {


    // Received on initial subscription
    case state : CurrentClusterState  =>

      // Register at master
      state.members.filter(_.status == MemberStatus.Up).foreach { member =>

        if(member.hasRole("master")) {

              context.actorSelection(RootActorPath(member.address) / "user" / "master")
        }

      }




  }




}



/*

  def receive = {
    case TransformationJob(text) => sender() ! TransformationResult(text.toUpperCase)
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up) foreach register
    case MemberUp(m) => register(m)
  }

  def register(member: Member): Unit =
    if (member.hasRole("frontend"))
      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") !
        BackendRegistration
}


 */

