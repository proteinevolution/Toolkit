package de.proteinevolution.results.db

import javax.inject.{ Inject, Singleton }
import play.api.cache.{ AsyncCacheApi, NamedCache }
import better.files._
import de.proteinevolution.jobs.services.JobFolderValidation
import de.proteinevolution.models.ConstantsV2
import play.api.Logger

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import io.circe.syntax._
import io.circe.Json
import io.circe.parser._

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
            (constants.jobPath / jobID / "results").list.withFilter(_.extension.contains(".json")).toList
          logger.info(s"Loading files for $jobID: ${files.map(_.name).mkString(",")}")
          val results: Json =
            files
              .map { file =>
                file.nameWithoutExtension -> parse(file.contentAsString).asJson
              }
              .toMap[String, Json]
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
