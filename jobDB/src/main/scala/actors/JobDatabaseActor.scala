package actors

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.Cluster
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe
import akkaguice.NamedActor
import database.{DBJob, JobDatabaseService}
import play.api.Logger


object JobDatabaseActor  extends NamedActor {

  override final val name = "JobDatabaseActor"
}


/**
  *
  * Created by lzimmermann on 28.04.16.
  */
class JobDatabaseActor @Inject() (jobDB : JobDatabaseService) extends Actor with ActorLogging {

  val cluster = Cluster.get(context.system)
  val mediator = DistributedPubSub(context.system).mediator


  override def preStart = {

    cluster.subscribe(self, classOf[MemberUp])
    mediator ! Subscribe("JOBS", self)
  }

  override def postStop = {

    cluster.unsubscribe(self)
  }

  def receive =  {

    // Received on initial subscription
    case state : CurrentClusterState  => //

    case ToolkitClusterEvent.JobStateChanged(jobID, sessionID,  newState, toolname) =>

       Logger.info("Hello from JOb " + jobID)
       log.info("Hello from Job " + jobID)

       jobDB.update(DBJob(jobID, sessionID, newState, toolname))

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

