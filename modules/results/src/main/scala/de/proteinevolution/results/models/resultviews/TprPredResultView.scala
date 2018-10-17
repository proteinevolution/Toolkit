package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.results.results.TPRPredResult

import scala.collection.immutable.ListMap

case class TprPredResultView(jobId: String, result: TPRPredResult) extends ResultView {

  override lazy val tabs = ListMap(ResultViews.RESULTS -> views.html.resultpanels.tprpred(jobId, result))

}
