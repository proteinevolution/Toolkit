package actors

import javax.inject.{Named, Inject, Singleton}

import actors.ESManager._
import actors.UserManager.MessageWithUserID
import akka.actor.{ActorLogging, Actor, ActorRef}
import models.Constants
import models.database.Job
import models.search.JobDAO
import modules.Common
import play.api.Logger
import play.api.i18n.MessagesApi
import play.modules.reactivemongo.{ReactiveMongoComponents, ReactiveMongoApi}
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

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
                               with Constants
                               with Common {

  def AutoComplete(userID : BSONObjectID, queryString : String) = {
    jobDao.findAutoCompleteJobID(queryString).map { richSearchResponse =>
      val jobIDEntries = richSearchResponse.suggestion("jobID")
      if (jobIDEntries.size > 0) {
        jobIDEntries.entry(queryString).optionsText.toList
      } else {
        List.empty[String]
      }
    }
  }

  def ElasticSearch(userID : BSONObjectID, queryString : String) = {
    jobDao.fuzzySearchJobID(queryString).flatMap { richSearchResponse =>
      if (richSearchResponse.totalHits > 0) {
        val jobIDEntries = richSearchResponse.getHits.getHits
        val mainIDs      = jobIDEntries.toList.map(hit => BSONObjectID.parse(hit.id).get)

        // Collect the list of jobs
        findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in"-> mainIDs)))
      } else {
        Future.successful(List.empty[Job])
      }
    }
  }

  def receive : Receive = {
    case AutoComplete(userID : BSONObjectID, queryString : String, element) =>
      println("Auto Complete Query: " + queryString)
      AutoComplete(userID, queryString).foreach { jobIDStrings =>
        userManager ! AutoCompleteReply(userID, jobIDStrings, element)
      }

    case ElasticSearch(userID : BSONObjectID, queryString : String, element) =>
      ElasticSearch(userID, queryString).foreach { jobList =>
        userManager ! SearchReply(userID, jobList, element)
      }
    case SearchForHash(userID : BSONObjectID, query : String) =>

  }
}

object ESManager {

  /**
    * Incoming
    */
  case class AutoComplete(userID : BSONObjectID, queryString : String, element : Int) extends MessageWithUserID
  case class ElasticSearch(userID : BSONObjectID, queryString : String, element : Int) extends MessageWithUserID
  case class SearchForHash(userID : BSONObjectID, queryString : String) extends MessageWithUserID

  /**
    * Outgoing
    */
  case class AutoCompleteReply(userID : BSONObjectID, suggestionList : List[String], element : Int) extends MessageWithUserID
  case class SearchReply(userID : BSONObjectID, jobList : List[Job], element : Int) extends MessageWithUserID
  case class SearchForHashReply(userID : BSONObjectID, jobList : List[Job]) extends MessageWithUserID
}