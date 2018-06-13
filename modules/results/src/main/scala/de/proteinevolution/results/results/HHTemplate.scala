package de.proteinevolution.results.results

trait HHTemplate {
  def accession: String
}

object HHTemplate {
  case class DummyTemplate(accession: String = "foo") extends HHTemplate
}
