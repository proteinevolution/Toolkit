package actors

import javax.inject.{Named, Inject, Singleton}

import actors.ESManager.{SearchForHash, AutoCompleteReply, AutoComplete}
import actors.UserManager.MessageWithUserID
import akka.actor.{ActorLogging, Actor, ActorRef}
import models.Constants
import models.database.Job
import models.search.JobDAO
import play.api.i18n.MessagesApi
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.bson.BSONObjectID
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by astephens on 02.09.16.
  */
@Singleton
final class ESManager @Inject()(val messagesApi      : MessagesApi,
                                val reactiveMongoApi : ReactiveMongoApi,
              @Named("userManager") userManager      : ActorRef,
                                val jobDao           : JobDAO)
                            extends Actor
                               with ActorLogging
                               with ReactiveMongoComponents
                               with Constants {
  def receive : Receive = {
    case AutoComplete(userID : BSONObjectID, queryString : String) =>
      println("autoCompleteQuery: ")
      jobDao.findAutoComplete(queryString).foreach { rsr =>
        val jobIDEntries = rsr.suggestion("jobID")
        if (jobIDEntries.size > 0) {
          userManager ! AutoCompleteReply(userID, jobIDEntries.entry(queryString).optionsText.toList)
        }
      }
    case SearchForHash(userID : BSONObjectID, query : String) =>

  }
}

object ESManager {

  /**
    * Incoming
    */
  case class AutoComplete(userID : BSONObjectID, queryString : String) extends MessageWithUserID
  case class SearchForHash(userID : BSONObjectID, queryString : String) extends MessageWithUserID

  /**
    * Outgoing
    */
  case class AutoCompleteReply(userID : BSONObjectID, suggestionList : List[String]) extends MessageWithUserID
  case class SearchForHashReply(userID : BSONObjectID, jobList : List[Job]) extends MessageWithUserID
}