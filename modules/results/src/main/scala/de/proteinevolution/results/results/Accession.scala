package de.proteinevolution.results.results

import de.proteinevolution.results.results.HHBlits.HHBlitsHSP
import de.proteinevolution.results.results.HHPred.HHPredHSP
import de.proteinevolution.results.results.HHomp.HHompHSP
import de.proteinevolution.results.results.Hmmer.HmmerHSP
import de.proteinevolution.results.results.PSIBlast.PSIBlastHSP
import simulacrum._

@typeclass trait Accession[A] {
  def value(a: A): String
}

object Accession {
  implicit val hmmerValue: Accession[HmmerHSP] = new Accession[HmmerHSP] {
    def value(a: HmmerHSP): String = a.accession
  }
  implicit val hhpredValue: Accession[HHPredHSP] = new Accession[HHPredHSP] {
    def value(a: HHPredHSP): String = a.template.accession
  }
  implicit val hhblitsValue: Accession[HHBlitsHSP] = new Accession[HHBlitsHSP] {
    def value(a: HHBlitsHSP): String = a.template.accession
  }
  implicit val hhompValue: Accession[HHompHSP] = new Accession[HHompHSP] {
    def value(a: HHompHSP): String = a.template.accession
  }
  implicit val psiBlastValue: Accession[PSIBlastHSP] = new Accession[PSIBlastHSP] {
    def value(a: PSIBlastHSP): String = a.template.accession
  }
}
