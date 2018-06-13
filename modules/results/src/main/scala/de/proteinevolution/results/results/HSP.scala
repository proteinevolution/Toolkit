package de.proteinevolution.results.results

import play.api.libs.json.JsValue

trait HSP {

  def description: String

  def toDataTable(db: String): JsValue

  def info: SearchToolInfo

  def num: Int

  def evalue: Double

  def accession: String

  def template: HHTemplate

}
