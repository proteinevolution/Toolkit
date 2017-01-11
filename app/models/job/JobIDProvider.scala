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

}
