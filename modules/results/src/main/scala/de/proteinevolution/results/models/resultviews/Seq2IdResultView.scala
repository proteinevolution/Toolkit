package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews
import play.api.libs.json.JsValue

import scala.collection.immutable.ListMap

case class Seq2IdResultView(jobId: String, result: JsValue) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels.unchecked_list("seq2id",
                                                                  jobId,
                                                                  result)
  )

}
