package de.proteinevolution.tools.results

import play.api.libs.json.JsValue

trait SearchTool[+T] {

  def parseResult(jsValue: JsValue): SearchResult[T]

}
