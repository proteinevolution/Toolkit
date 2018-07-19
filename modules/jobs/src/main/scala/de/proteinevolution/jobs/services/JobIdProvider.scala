package de.proteinevolution.jobs.services

import de.proteinevolution.jobs.dao.JobDao
import javax.inject.{ Inject, Singleton }

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Random
import cats.effect.IO

@Singleton
class JobIdProvider @Inject()(
    jobDao: JobDao,
    @volatile private var usedIds: ListBuffer[String] = new ListBuffer[String]()
)(implicit ec: ExecutionContext) {

  def provide: Future[String] = generate.unsafeToFuture()

  @inline def trash(id: String): Unit = {
    usedIds = usedIds - id
  }

  private def generate: IO[String] = {
    val id =
      Iterator.continually[String](Random.nextInt(9999999).toString.padTo(7, '0')).filterNot(usedIds.contains).next()
    validate(id).flatMap { b =>
      if (b) IO.pure(id) else generate
    }
  }

  private def isValid(id: String): Future[Boolean] = {
    jobDao.selectJob(id).map(_.isEmpty)
  }

  private def validate(id: String): IO[Boolean] = {
    IO.fromFuture(IO.pure(isValid(id)))
  }

}
