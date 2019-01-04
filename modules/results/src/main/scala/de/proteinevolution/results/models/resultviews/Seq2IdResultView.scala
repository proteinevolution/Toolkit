package de.proteinevolution.results.models.resultviews

import de.proteinevolution.results.results.Unchecked
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class Seq2IdResultView(jobId: String, result: Unchecked) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.unchecked_list("seq2id", jobId, result)
  )

}
