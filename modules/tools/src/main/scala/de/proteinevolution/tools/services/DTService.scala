package de.proteinevolution.tools.services

import javax.inject.Singleton

import de.proteinevolution.tools.results.General.DTParam
import de.proteinevolution.tools.results.{ HSP, SearchResult }

@Singleton
class DTService {

  def getHitsByKeyWord[T <: HSP](hits: SearchResult[T], params: DTParam): List[T] = {
    val hitList = hits.hitsOrderBy(params)
    if (params.searchValue.length > 0) {
      hitList.filter(
        hit => (hit.description + hit.template.accession).toUpperCase.contains(params.searchValue.toUpperCase)
      )
    } else {
      hitList
    }
  }

}
