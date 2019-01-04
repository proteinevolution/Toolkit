package de.proteinevolution.results.models.resultviews

import de.proteinevolution.results.results.TPRPredResult
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class TprPredResultView(jobId: String, result: TPRPredResult) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.tprpred(jobId, result)
  )

}
