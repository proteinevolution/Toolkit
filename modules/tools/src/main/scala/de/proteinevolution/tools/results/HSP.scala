package de.proteinevolution.tools.results

import play.api.libs.json.JsValue

trait HSP {

  def description: String

  def toDataTable(db: String): JsValue
}



