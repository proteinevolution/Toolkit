package de.proteinevolution.base

import play.api.i18n.I18nSupport
import play.api.mvc.{ AbstractController, ControllerComponents, Result }

abstract class ToolkitController(cc: ControllerComponents) extends AbstractController(cc) with I18nSupport {

  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate",
    EXPIRES       -> "0"
  )

}
