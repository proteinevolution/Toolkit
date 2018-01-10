package de.proteinevolution.tools.models

import javax.inject.Inject

import de.proteinevolution.tools.results._

case class ResultContext @Inject()(
    hhblits: HHBlits,
    hmmer: Hmmer,
    psiblast: PSIBlast,
    hhomp: HHomp,
    hhpred: HHPred
)
