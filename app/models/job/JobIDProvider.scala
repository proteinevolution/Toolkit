package models.job

import java.util.UUID
import javax.inject.{Named, Inject, Singleton}

import actors.JobIDActor.{GetId, GetIdReply}
import akka.actor.ActorRef
import akka.util.Timeout
import com.google.inject.ImplementedBy
import models.search.JobDAO
import modules.CommonModule
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.Future
import scala.concurrent._
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scalaz.concurrent.Task


import akka.pattern.ask

/**
  * Created by lzimmermann on 02.12.16.
  */

@ImplementedBy(classOf[JobIDProviderImpl])
sealed trait JobIDProvider {

  def provide: Future[String]
}

@Singleton
final class JobIDProviderImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                        val jobDao : JobDAO,
                                        @Named("jobIDActor") repo : ActorRef) extends JobIDProvider with CommonModule {

  case class JobIdException(message: String = "", cause: Throwable = null) extends Exception(message, cause)


  private[this] def candIt : Iterator[Set[String]] =
    Iterator.continually[Set[String]](Stream.continually(Random.nextInt(9999999).toString.padTo(7, '0')).take(100).toSet)




  // Gives the first jobID which does not already exist in the database


  def provideTry: Future[String] = {


    val set = candIt.next()

    jobDao.multiExistsJobID(set).map { richSearchResponse =>

        lazy val foundIds = for {x <- richSearchResponse.getHits.hits()} yield x.getSource.get("jobID").toString

        val usedSet = foundIds.toSet // exclude these jobIds from being reused again

        lazy val okSet = set.diff(usedSet) // clean set



        if(okSet.isEmpty)

          throw JobIdException("generated jobIds already taken, retry...") // this exception is thrown to start the retry

        else

          okSet.head

      }

  }


  val provideTask: Task[Future[String]] = Task.delay(provideTry)

  val retryTask: Task[Future[String]] = provideTask.retry(Seq(1.millis, 1.millis, 1.millis, 1.millis)) // retries 4 times

  def provide: Future[String] = retryTask.unsafePerformSync




  private def getUniqueId(): Future[String] = {
    implicit val timeout = Timeout(500.millis)
    val id = UUID.randomUUID().toString
    repo ? GetId(id) flatMap {
      case GetIdReply(Some(_)) => getUniqueId()
      case GetIdReply(None) => Future.successful(id)
    }
  }


 /*

  // -- alternative solution:

  implicit val timeout = Timeout(1.millis)

  // checks elasticsearch whether the string exists in the collection

  def checkES(id : String) : Boolean = {


    val result = jobDao.existsJobID(id).map { richSearchResponse =>

      println("TEST" + richSearchResponse.totalHits + "for " + id)
      if(richSearchResponse.totalHits == 0) {
        false
      }

      else
        true

    }

    Await.result(result,timeout.duration)

  }

  val x : Iterator[String] = Iterator.continually(Random.nextInt(9999999).toString.padTo(7, '0')).filterNot(x => checkES(x))


  def provide2 : Future[String] = {

    Future {
      x.next()
    }

  } */

}