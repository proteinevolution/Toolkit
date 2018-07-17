package de.proteinevolution.results.services

import de.proteinevolution.results.results.General.DTParam
import de.proteinevolution.results.results.Hmmer.HmmerHSP
import de.proteinevolution.results.results.{ HSP, SearchResult }
import scala.reflect.runtime.universe._

trait DTService {

  def getHitsByKeyWord[T <: HSP](hits: SearchResult[T], params: DTParam)(implicit tool: TypeTag[T]): List[T] = {
    val hitList = hits.hitsOrderBy(params)
    if (params.searchValue.length > 0) {
      hitList.filter { hit =>
        val accession = tool match {
          case t if t == typeTag[HmmerHSP] => hit.accession
          case _                           => hit.template.accession
        }
        (hit.description + accession).toUpperCase.contains(params.searchValue.toUpperCase)
      }
    } else {
      hitList
    }
  }

}
