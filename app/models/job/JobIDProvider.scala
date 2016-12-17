package models.job

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import modules.CommonModule
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.Future
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by lzimmermann on 02.12.16.
  */
@ImplementedBy(classOf[JobIDProviderImpl])
sealed trait JobIDProvider {

  def provide: Future[String]
}

@Singleton
class JobIDProviderImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends JobIDProvider with CommonModule {

  private val candIt = Iterator.continually[Set[String]](Stream.continually(Random.nextInt(9999999).toString.padTo(7, '0')).take(100).toSet)

  // Gives the first jobID which does not already exist in the database
  // TODO This can fail in some cases
  def provide: Future[String] = {

    val set = candIt.next()

    selectJobs(set).map(ret =>

      set.diff(ret.map(_.jobID)).head
    )
  }
}
