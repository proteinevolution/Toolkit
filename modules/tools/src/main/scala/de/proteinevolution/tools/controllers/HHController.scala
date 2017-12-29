package de.proteinevolution.tools.controllers

import javax.inject.Inject

import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.models.ToolNames
import de.proteinevolution.tools.models.{ HHContext, ResultContext }
import de.proteinevolution.tools.results.General.DTParam
import de.proteinevolution.tools.results.HHBlits.HHBlitsHSP
import de.proteinevolution.tools.results.HHPred.HHPredHSP
import de.proteinevolution.tools.results.HHomp.HHompHSP
import de.proteinevolution.tools.results.Hmmer.HmmerHSP
import de.proteinevolution.tools.results.PSIBlast.PSIBlastHSP
import de.proteinevolution.tools.services.{ DTService, ToolNameGetService }
import play.api.libs.json.{ JsObject, Json }
import play.api.mvc.{ AbstractController, Action, AnyContent }
import scala.concurrent.ExecutionContext

class HHController @Inject()(ctx: HHContext,
                             resultCtx: ResultContext,
                             toolFinder: ToolNameGetService,
                             dtService: DTService,
                             resultFiles: ResultFileAccessor)(implicit ec: ExecutionContext)
    extends AbstractController(ctx.controllerComponents) {

  def loadHits = ???

  def dataTable(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val params = DTParam(
      request.getQueryString("sSearch").getOrElse(""),
      request.getQueryString("iDisplayStart").getOrElse("0").toInt,
      request.getQueryString("iDisplayLength").getOrElse("100").toInt,
      request.getQueryString("iSortCol_0").getOrElse("1").toInt,
      request.getQueryString("sSortDir_0").getOrElse("asc")
    )

    resultFiles.getResults(jobID).map {
      case None => NotFound
      case Some(jsValue) =>
        val result = resultCtx.hhpred.parseResult(jsValue)
        val h = toolFinder.getTool(jobID).map {
          case x if x == ToolNames.HHBLITS =>
            dtService.getHitsByKeyWord[HHBlitsHSP](resultCtx.hhblits.parseResult(jsValue), params)
          case x if x == ToolNames.HHOMP =>
            dtService.getHitsByKeyWord[HHompHSP](resultCtx.hhomp.parseResult(jsValue), params)
          case x if x == ToolNames.HHPRED =>
            dtService.getHitsByKeyWord[HHPredHSP](resultCtx.hhpred.parseResult(jsValue), params)
          case x if x == ToolNames.HMMER =>
            dtService.getHitsByKeyWord[HmmerHSP](resultCtx.hmmer.parseResult(jsValue), params)
          case x if x == ToolNames.PSIBLAST =>
            dtService.getHitsByKeyWord[PSIBlastHSP](resultCtx.psiBlast.parseResult(jsValue), params)
          case _ => throw new IllegalArgumentException("datatables not implemented for this tool")
        }

        println(h)

        val hits = dtService.getHitsByKeyWord[HHPredHSP](result, params)

        Ok(
          Json
            .toJson(Map("iTotalRecords" -> result.num_hits, "iTotalDisplayRecords" -> result.num_hits))
            .as[JsObject]
            .deepMerge(Json.obj("aaData" -> hits.map(_.toDataTable(result.db))))
        )
    }
  }

  def eval = ???

  def aln = ???

  def evalFull = ???

  def full = ???

}
