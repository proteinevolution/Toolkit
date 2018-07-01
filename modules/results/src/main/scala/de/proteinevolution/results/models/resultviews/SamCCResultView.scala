package de.proteinevolution.results.models.resultviews

case class SamCCResultView(jobId: String) extends ResultView {

  override lazy val tabs = Map(
    "3D-Structure-With-Axes" ->
    views.html.resultpanels.NGL3DStructure(
      s"/results/files/$jobId/$jobId.pdb",
      jobId + ".pdb",
      jobId,
      "samcc_PDB_AXES"
    ),
    "Plots" -> views.html.resultpanels.samcc(
      s"/results/files/$jobId/out0.png",
      s"/results/files/$jobId/out1.png",
      s"/results/files/$jobId/out2.png",
      s"/results/files/$jobId/out3.png"
    ),
    "NumericalData" -> views.html.resultpanels.fileviewWithDownload(jobId + ".out", jobId, "samcc")
  )

}
