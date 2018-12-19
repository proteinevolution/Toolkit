package de.proteinevolution.results.results

import simulacrum._

@typeclass trait Accession[A] {
  def value(a: A): String
}

object Accession {
  implicit val hmmerValue: Accession[HmmerHSP]       = (a: HmmerHSP) => a.accession
  implicit val hhpredValue: Accession[HHPredHSP]     = (a: HHPredHSP) => a.template.accession
  implicit val hhblitsValue: Accession[HHBlitsHSP]   = (a: HHBlitsHSP) => a.template.accession
  implicit val hhompValue: Accession[HHompHSP]       = (a: HHompHSP) => a.template.accession
  implicit val psiBlastValue: Accession[PSIBlastHSP] = (a: PSIBlastHSP) => a.accession
}
