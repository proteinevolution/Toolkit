package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ConstantsV2

import scala.collection.immutable.ListMap

case class ModellerResultView(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs = ListMap(
    "3D-Structure" -> views.html.resultpanels.NGL3DStructure(
      s"/results/files/$jobId/$jobId.pdb",
      jobId + ".pdb",
      jobId,
      "Modeller"
    )
  )
}
