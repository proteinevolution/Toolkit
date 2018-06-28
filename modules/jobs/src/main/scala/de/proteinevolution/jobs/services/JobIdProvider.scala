package de.proteinevolution.jobs.services

import de.proteinevolution.db.MongoStore
import javax.inject.{ Inject, Singleton }

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.Random

@Singleton
class JobIdProvider @Inject()(
    mongoStore: MongoStore,
    private var usedIds: ListBuffer[String] = new ListBuffer[String]()
)(implicit ec: ExecutionContext) {

  private def isValid(id: String): Future[Boolean] = {
    mongoStore.selectJob(id).map(_.isEmpty)
  }

  def provide: String = {
    val id = Iterator
      .continually[String](Random.nextInt(9999999).toString.padTo(7, '0'))
      .filter(x => Await.result(isValid(x), scala.concurrent.duration.Duration.Inf))
      .filterNot(usedIds.contains)
      .next()
    usedIds += id
    id
  }

  def trash(id: String): Unit = {
    usedIds = usedIds - id
  }

}
