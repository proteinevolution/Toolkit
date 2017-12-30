package de.proteinevolution.tools.results

import de.proteinevolution.tools.results.General.DTParam

trait SearchResult[+T] {

  def hitsOrderBy(params: DTParam): List[T]

  def num_hits: Int

  def db: String

  def HSPS: List[T]

}