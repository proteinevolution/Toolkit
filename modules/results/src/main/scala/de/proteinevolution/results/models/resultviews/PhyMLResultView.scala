package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ConstantsV2
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class PhyMLResultView(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    TREE -> views.html.resultpanels.tree(
      jobId + ".phy_phyml_tree.txt",
      s"${constants.jobPath}$jobId/results/" + jobId + ".phy_phyml_tree.txt",
      jobId,
      "PhyML"
    ),
    DATA -> views.html.resultpanels.fileviewWithDownload(jobId + ".stats", jobId, "phyml_data")
  )

}
