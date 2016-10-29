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
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.ExecutionContext.Implicits.global
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
                               with Constants {

  def jobBSONCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs"))

  def receive : Receive = {
    case AutoComplete(userID : BSONObjectID, queryString : String, element) =>
      println("Auto Complete Query: " + queryString)
      jobDao.findAutoCompleteJobID(queryString).foreach { rsr =>
        val jobIDEntries = rsr.suggestion("jobID")
        if (jobIDEntries.size > 0) {
          userManager ! AutoCompleteReply(userID, jobIDEntries.entry(queryString).optionsText.toList, element)
        }
      }

    case ElasticSearch(userID : BSONObjectID, queryString : String, element) =>
      jobDao.fuzzySearchJobID(queryString).foreach { richSearchResponse =>
        if (richSearchResponse.totalHits > 0) {
          val jobIDEntries = richSearchResponse.getHits.getHits
          val mainIDs      = jobIDEntries.toList.map(hit => BSONObjectID.parse(hit.id).get)
          val futureJobs   = jobBSONCollection.map(_.find(BSONDocument(Job.IDDB ->
                                                          BSONDocument("$in"    -> mainIDs))).cursor[Job]())

          jobBSONCollection
          // Collect the list and then create the reply
          futureJobs.flatMap{ _.collect[List](-1, Cursor.FailOnError[List[Job]]())
          }.andThen {
            case Success(jobList) =>
              println("Found " + jobList.length.toString + " Job[s]. Sending.")
              userManager ! SearchReply(userID, jobList, element)
            case Failure(error) =>
              println(error.toString)
          }
        } else {
          userManager ! SearchReply(userID, List.empty, element)
        }
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