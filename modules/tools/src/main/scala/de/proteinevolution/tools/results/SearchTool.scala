package de.proteinevolution.tools.results

import play.api.libs.json.JsValue

trait SearchTool {

  def parseResult(jsValue: JsValue): SearchResult

}
