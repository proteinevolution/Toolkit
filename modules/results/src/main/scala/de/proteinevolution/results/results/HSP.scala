package de.proteinevolution.results.results

import io.circe.Json

trait HSP {

  def description: String

  def toDataTable(db: String): Json

  def info: Option[SearchToolInfo]

  def num: Int

  def eValue: Double

  def accession: String

  def template: Option[HHTemplate]

}
