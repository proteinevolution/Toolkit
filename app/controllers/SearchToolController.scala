package controllers

import javax.inject.Inject

import de.proteinevolution.tools.results.HHBlits.HHBlitsResult
import de.proteinevolution.tools.results.SearchTool
import play.api.mvc.{ AbstractController, Action, AnyContent }

import scala.concurrent.ExecutionContext

abstract class SearchToolController @Inject()(ctx: SearchToolContext, searchTool: SearchTool)(
    implicit ec: ExecutionContext
) extends AbstractController(ctx.controllerComponents) {

  def loadHits(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val start   = (json \ "start").as[Int]
    val end     = (json \ "end").as[Int]
    val wrapped = (json \ "wrapped").as[Boolean]
    ctx.resultFiles.getResults(jobID).map {
      case None => NotFound
      case Some(jsValue) =>
        val result = searchTool.parseResult(jsValue).asInstanceOf[HHBlitsResult]
        if (end > result.num_hits || start > result.num_hits) {
          BadRequest
        } else {
          val hits =
            result.HSPS.slice(start, end).map { views.html.jobs.resultpanels.hhblits.hit(_, wrapped) }
          Ok(hits.mkString)
        }
    }
  }

}
