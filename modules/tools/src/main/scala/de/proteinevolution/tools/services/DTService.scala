package de.proteinevolution.tools.services

import javax.inject.Singleton

import de.proteinevolution.tools.results.General.DTParam
import de.proteinevolution.tools.results.{ HSP, SearchResult }

@Singleton
class DTService {

  def getHitsByKeyWord[T <: HSP](hits: SearchResult[T], params: DTParam): List[T] = {
    if (params.sSearch.isEmpty) {
      hits.hitsOrderBy(params).slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
    } else {
      hits
        .hitsOrderBy(params)
        .filter(hit => (hit.description + hit.template.accession).toUpperCase.contains(params.sSearch.toUpperCase))
    }
  }

}
