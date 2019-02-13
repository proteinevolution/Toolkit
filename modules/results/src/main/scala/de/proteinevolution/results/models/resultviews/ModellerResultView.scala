package de.proteinevolution.results.models.resultviews

import de.proteinevolution.common.models.ConstantsV2
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class ModellerResultView(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    "3D-Structure" -> views.html.resultpanels.NGL3DStructure(
      s"/results/files/$jobId/$jobId.pdb",
      jobId + ".pdb",
      jobId,
      "Modeller"
    )
  )
}
