package de.proteinevolution.results.models

import de.proteinevolution.results.results._
import javax.inject.Inject

case class ResultContext @Inject()(
    hhblits: HHBlits,
    hmmer: Hmmer,
    psiblast: PSIBlast,
    hhomp: HHomp,
    hhpred: HHPred
)
