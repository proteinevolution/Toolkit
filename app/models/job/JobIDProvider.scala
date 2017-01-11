package models.job

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import models.search.JobDAO
import modules.CommonModule
import play.modules.reactivemongo.ReactiveMongoApi
import scala.concurrent.Future
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scalaz.concurrent.Task

/**
  * Created by lzimmermann on 02.12.16.
  */

@ImplementedBy(classOf[JobIDProviderImpl])
sealed trait JobIDProvider {

  def provide: Future[String]
}

@Singleton
class JobIDProviderImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                                  val jobDao : JobDAO) extends JobIDProvider with CommonModule {

  case class CustomException(message: String = "", cause: Throwable = null)
    extends Exception(message, cause)

  private[this] def candIt : Iterator[Set[String]] =
    Iterator.continually[Set[String]](Stream.continually(Random.nextInt(9999999).toString.padTo(7, '0')).take(100).toSet)

  // Gives the first jobID which does not already exist in the database


  def provideTry: Future[String] = {

    var okSet = Set.empty[String]

    val set = candIt.next()

      jobDao.multiExistsJobID(set).map { richSearchResponse =>

        val foundIds = for {x <- richSearchResponse.getHits.hits()} yield x.getSource.get("jobID").toString

        val usedSet = foundIds.toSet // exclude those jobIds from being reused again

        okSet = set.diff(usedSet) // clean set

        if(okSet.isEmpty)
          throw CustomException("generated Ids already taken, retry...")
        else
          okSet.head

      }

  }


  val provideTask: Task[Future[String]] = Task.delay(provideTry)

  val retryTask: Task[Future[String]] = provideTask.retry(Seq(5.millis, 10.millis, 15.millis, 20.seconds)) // retries 4 times

  def provide: Future[String] = retryTask.unsafePerformSync

}
