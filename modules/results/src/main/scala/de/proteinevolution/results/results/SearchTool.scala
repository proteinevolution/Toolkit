package de.proteinevolution.results.results

import play.api.libs.json.JsValue
import simulacrum._

@typeclass trait SearchTool[+T] {

  def parseResult(jsValue: JsValue): SearchResult[T]

}
