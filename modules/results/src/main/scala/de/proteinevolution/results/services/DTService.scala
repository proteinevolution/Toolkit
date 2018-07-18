package de.proteinevolution.results.services

import de.proteinevolution.results.results.General.DTParam
import de.proteinevolution.results.results.{ Accession, HSP, SearchResult }

trait DTService {

  def getHitsByKeyWord[T <: HSP](hits: SearchResult[T], params: DTParam)(implicit accession: Accession[T]): List[T] = {
    val hitList = hits.hitsOrderBy(params)
    if (params.searchValue.length > 0) {
      hitList.filter { hit =>
        (hit.description + accession.value(hit)).toUpperCase.contains(params.searchValue.toUpperCase)
      }
    } else {
      hitList
    }
  }

}
