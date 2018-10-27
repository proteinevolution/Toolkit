package de.proteinevolution.results.db

import better.files._
import de.proteinevolution.jobs.services.JobFolderValidation
import de.proteinevolution.models.ConstantsV2
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.cache.{ AsyncCacheApi, NamedCache }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class ResultFileAccessor @Inject()(
    constants: ConstantsV2,
    @NamedCache("resultCache") resultCache: AsyncCacheApi
)(implicit ec: ExecutionContext)
    extends JobFolderValidation {

  private val logger = Logger(this.getClass)

  def getResults(jobID: String): Future[Option[Json]] = {
    resultCache.get[Json](jobID).map {
      case Some(resultMap) =>
        Some(resultMap)
      case None =>
        if (resultsExist(jobID, constants)) {
          val files: List[File] =
            File(s"${constants.jobPath}/$jobID/results").list.withFilter(_.extension.contains(".json")).toList
          logger.info(s"Loading files for $jobID: ${files.map(_.name).mkString(",")}")
          val results: Json =
            files.map { file =>
                file.nameWithoutExtension -> parse(file.contentAsString).right.toOption.getOrElse {
                  logger.error("Invalid result json from database")
                  throw new NoSuchElementException
                }
            }.toMap[String, Json]
              .updated("jobID", jobID.asJson)
              .asJson
          resultCache.set(jobID, results, 10.minutes)
          Some(results)
        } else {
          None
        }
    }
  }

}
