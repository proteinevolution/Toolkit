package actors

import javax.inject.{Named, Inject}

import actors.JobMonitor.UpdateJobStatus
import akka.actor.{ActorLogging, ActorRef, Actor}
import controllers.Settings
import models.{ExitCodes, Constants}
import models.database._
import models.search.JobDAO
import modules.CommonModule
import modules.tel.TEL
import play.api.i18n.MessagesApi
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import javax.inject.Singleton

import reactivemongo.bson.BSONObjectID

/**
 * Real time logging of jobstate transitions
 * Created by snam on 11.11.16.
 */


object JobMonitor {

  case class UpdateJobStatus(job : BSONObjectID, status : JobState)

}


@Singleton
final class JobMonitor @Inject() (val messagesApi: MessagesApi,
                                  val reactiveMongoApi: ReactiveMongoApi,
                                  @Named("userManager") userManager : ActorRef,
                                  val tel : TEL,
                                  val jobDao : JobDAO,
                                  val settings : Settings,
                                  implicit val materializer: akka.stream.Materializer)
  extends Actor with ActorLogging with ReactiveMongoComponents with Constants with ExitCodes with CommonModule {



  def receive :Receive = {

    case UpdateJobStatus(job: BSONObjectID, status: JobState) =>


  }

}


