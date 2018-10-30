package de.proteinevolution.base.controllers

import de.proteinevolution.base.helpers.ToolkitTypes
import io.circe.{ Json, JsonObject }
import io.circe.syntax._
import play.api.i18n.I18nSupport
import play.api.libs.circe.Circe
import play.api.mvc.{ AbstractController, ControllerComponents, Result }

abstract class ToolkitController(cc: ControllerComponents)
    extends AbstractController(cc)
    with I18nSupport
    with Circe
    with ToolkitTypes {

  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate",
    EXPIRES       -> "0"
  )

  protected def errors(names: String*): Json = {
    JsonObject.fromMap(names.map(_ -> Json.fromString("invalid")).toMap).asJson
  }

}
