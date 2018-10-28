package de.proteinevolution.results

import de.proteinevolution.results.controllers._
import de.proteinevolution.results.services.ForwardModeExtractor
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class ResultsRouter @Inject()(
    hhController: HHController,
    processController: ProcessController,
    alignmentController: AlignmentController,
    fileController: FileController,
    forwardModalController: ForwardModalController,
    resultGetController: ResultGetController
) extends SimpleRouter
    with ForwardModeExtractor {

  def routes: Routes = {
    case POST(p"/loadHits/$jobID")       => hhController.loadHits(jobID)
    case GET(p"/dataTable/$jobID")       => hhController.dataTable(jobID)
    case GET(p"/getStructure/$filename") => fileController.getStructureFile(filename)
    case POST(p"/forwardAlignment/$jobID/${forwardModeExtractor(mode) }") =>
      processController.forwardAlignment(jobID, mode)
    case GET(p"/templateAlignment/$jobID/$accession") => processController.templateAlignment(jobID, accession)
    case POST(p"/alignment/getAln/$jobID")            => alignmentController.getAln(jobID)
    case POST(p"/alignment/loadHits/$jobID")          => alignmentController.loadHits(jobID)
    case POST(p"/alignment/clustal/$jobID")           => alignmentController.loadHitsClustal(jobID)
    case GET(p"/files/$jobID/$filename")             => fileController.file(filename = filename, jobID = jobID)
    case GET(p"/forward/modal/$toolName/$modalType") =>
      forwardModalController.getForwardModalOptions(modalType, toolName)
    case GET(p"/$jobID/$tool/$view") => resultGetController.get(jobID, tool, view)
    case GET(p"/job/$jobID")         => resultGetController.getJob(jobID)
  }

}
