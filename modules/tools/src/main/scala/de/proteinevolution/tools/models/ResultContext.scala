package de.proteinevolution.tools.models

import javax.inject.Inject

import de.proteinevolution.tools.results._

case class ResultContext @Inject()(
  hhblits: HHBlits, hmmer: Hmmer, psiBlast: PSIBlast, hhomp: HHomp, hhpred: HHPred
)
