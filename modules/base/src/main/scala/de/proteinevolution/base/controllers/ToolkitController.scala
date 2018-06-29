package de.proteinevolution.base.controllers

import play.api.i18n.I18nSupport
import play.api.libs.json.{ __, JsError, JsValue, JsonValidationError }
import play.api.mvc.{ AbstractController, ControllerComponents, Result }

abstract class ToolkitController(cc: ControllerComponents) extends AbstractController(cc) with I18nSupport {

  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate",
    EXPIRES       -> "0"
  )

  protected def errors(names: String*): JsValue = {
    JsError.toJson(JsError(names.map { name =>
      (__ \ name, JsonValidationError("invalid") :: Nil)
    }))
  }

}
