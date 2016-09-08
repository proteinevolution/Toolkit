package actors

import javax.inject.{Named, Inject, Singleton}

import actors.ESManager._
import actors.UserManager.MessageWithUserID
import akka.actor.{ActorLogging, Actor, ActorRef}
import models.Constants
import models.database.Job
import models.search.JobDAO
import play.api.Logger
import play.api.i18n.MessagesApi
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
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

  def jobBSONCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs"))

  def receive : Receive = {
    case AutoComplete(userID : BSONObjectID, queryString : String) =>
      println("Auto Complete Query: " + queryString)
      jobDao.findAutoComplete(queryString).foreach { rsr =>
        val jobIDEntries = rsr.suggestion("jobID")
        if (jobIDEntries.size > 0) {
          userManager ! AutoCompleteReply(userID, jobIDEntries.entry(queryString).optionsText.toList)
        }
      }

    case Search(userID : BSONObjectID, queryString : String) =>
      jobDao.findAutoComplete(queryString).foreach { rsr =>
        val jobIDEntries = rsr.getHits.getHits
        val mainIDs      = jobIDEntries.toList.map(hit => BSONObjectID(hit.getId))
        val futureJobs   = jobBSONCollection.map(_.find(BSONDocument(Job.IDDB ->
                                                        BSONDocument("$in" -> mainIDs))).cursor[Job]())

        // Collect the list and then create the reply
        futureJobs.flatMap(_.collect[List]()).foreach { jobList =>
          //println("Found " + jobList.length.toString + " Job[s]. Sending.")
          userManager ! SearchReply(userID, jobList)
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
  case class Search(userID : BSONObjectID, queryString : String) extends MessageWithUserID
  case class SearchForHash(userID : BSONObjectID, queryString : String) extends MessageWithUserID

  /**
    * Outgoing
    */
  case class AutoCompleteReply(userID : BSONObjectID, suggestionList : List[String]) extends MessageWithUserID
  case class SearchForHashReply(userID : BSONObjectID, jobList : List[Job]) extends MessageWithUserID
  case class SearchReply(userID : BSONObjectID, jobList : List[Job]) extends MessageWithUserID
}