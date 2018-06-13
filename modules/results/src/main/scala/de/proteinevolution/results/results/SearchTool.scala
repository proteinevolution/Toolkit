package de.proteinevolution.results.results

import play.api.libs.json.JsValue

trait SearchTool[+T] {

  def parseResult(jsValue: JsValue): SearchResult[T]

}
