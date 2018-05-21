package de.proteinevolution.db

import javax.inject.{ Inject, Singleton }

import play.api.cache.{ AsyncCacheApi, NamedCache }
import play.api.libs.json.JsValue
import better.files._
import de.proteinevolution.models.ConstantsV2
import play.api.Logger
import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

@Singleton
final class ResultFileAccessor @Inject()(
    constants: ConstantsV2,
    @NamedCache("resultCache") implicit val resultCache: AsyncCacheApi
)(implicit ec: ExecutionContext) {
  def getResults(jobID: String): Future[Option[JsValue]] = {
    resultCache.get[JsValue](jobID).map {
      case Some(resultMap) =>
        // Pull the results directly from the cache
        Some(resultMap)
      case None =>
        // check if the directories exist
        if ((constants.jobPath / jobID).exists && (constants.jobPath / jobID / "results").exists) {
          // Gather the files
          val files: List[File] =
            (constants.jobPath / jobID / "results").list.withFilter(_.extension.contains(".json")).toList
          Logger.info(s"[ResultFileAccessor] Loading files for $jobID: ${files.map(_.name).mkString(",")}")

          // Merge the results from the files
          val results: JsValue =
            Json.toJson(
              files
                .map { file =>
                  file.nameWithoutExtension -> Json.parse(file.contentAsString)
                }
                .toMap[String, JsValue]
                .updated("jobID", Json.toJson(jobID))
            )

          // Push them into the cache
          resultCache.set(jobID, results, 10.minutes)
          // show them to the user
          Some(results)
        } else {
          None
        }
    }
  }
}
