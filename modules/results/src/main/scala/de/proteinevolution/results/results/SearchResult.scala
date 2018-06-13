package de.proteinevolution.results.results

import de.proteinevolution.results.results.Alignment.AlignmentResult
import de.proteinevolution.results.results.General.DTParam

trait SearchResult[+T] {

  def hitsOrderBy(params: DTParam): List[T]

  def num_hits: Int

  def db: String

  def HSPS: List[T]

  def alignment: AlignmentResult

}
