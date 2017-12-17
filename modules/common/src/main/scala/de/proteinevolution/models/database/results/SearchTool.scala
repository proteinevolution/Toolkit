package de.proteinevolution.models.database.results

import play.api.libs.json.JsValue

trait SearchTool {

  def parseResult(jsValue: JsValue): SearchResult

}
