package de.proteinevolution.results.services

import de.proteinevolution.results.results.General.DTParam
import de.proteinevolution.results.results.{ HSP, SearchResult }
import javax.inject.Singleton

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
