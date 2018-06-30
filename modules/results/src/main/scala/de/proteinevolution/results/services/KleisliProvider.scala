package de.proteinevolution.results.services

import cats.data.Kleisli
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.models.ToolName
import javax.inject.Inject
import play.api.libs.json.JsValue

import scala.concurrent.Future

class KleisliProvider @Inject()(toolFinder: ToolNameGetService, resultFiles: ResultFileAccessor) {
  val resK: Kleisli[Future, String, Option[JsValue]] = Kleisli(resultFiles.getResults)
  val toolK: Kleisli[Future, String, ToolName]       = Kleisli(toolFinder.getTool)
}
