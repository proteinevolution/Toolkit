package de.proteinevolution.results.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.models.ToolName._
import de.proteinevolution.results.models.{ HHContext, ResultContext, ResultsForm }
import de.proteinevolution.results.results.General.DTParam
import de.proteinevolution.results.results.HHBlits.HHBlitsHSP
import de.proteinevolution.results.results.HHPred.HHPredHSP
import de.proteinevolution.results.results.HHomp.HHompHSP
import de.proteinevolution.results.results.Hmmer.HmmerHSP
import de.proteinevolution.results.results.PSIBlast.PSIBlastHSP
import de.proteinevolution.results.results.HSP
import de.proteinevolution.results.services.ResultsRepository.ResultsService
import de.proteinevolution.results.services.{ DTService, ResultsRepository, ToolNameGetService }
import javax.inject.{ Inject, Singleton }
import play.api.libs.json.{ JsObject, Json }
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext

@Singleton
class HHController @Inject()(
    ctx: HHContext,
    resultCtx: ResultContext,
    toolFinder: ToolNameGetService,
    resultFiles: ResultFileAccessor,
)(implicit ec: ExecutionContext)
    extends ToolkitController(ctx.controllerComponents)
    with ResultsRepository
    with DTService {

  private val resultsService = ResultsService(toolFinder, resultFiles)

  def loadHits(jobId: String): Action[ResultsForm] = Action(circe.json[ResultsForm]).async { implicit request =>
    val data    = request.body
    val wrapped = data.wrapped.getOrElse(false)
    val isColor = data.isColor.getOrElse(false)
    getResults(jobId)
      .run(resultsService)
      .flatMap {
        case Some(jsValue) =>
          getTool(jobId).run(resultsService).map {
            case HHBLITS =>
              (resultCtx.hhblits.parseResult(jsValue),
               (hsp: HSP) => views.html.hhblits.hit(hsp.asInstanceOf[HHBlitsHSP], wrapped, jobId))
            case HHPRED =>
              (resultCtx.hhpred.parseResult(jsValue),
               (hsp: HSP) => views.html.hhpred.hit(hsp.asInstanceOf[HHPredHSP], isColor, wrapped, jobId))
            case HHOMP =>
              (resultCtx.hhomp.parseResult(jsValue),
               (hsp: HSP) => views.html.hhomp.hit(hsp.asInstanceOf[HHompHSP], isColor, wrapped, jobId))
            case HMMER =>
              val result = resultCtx.hmmer.parseResult(jsValue)
              (result, (hsp: HSP) => views.html.hmmer.hit(hsp.asInstanceOf[HmmerHSP], result.db, wrapped))
            case PSIBLAST =>
              val result = resultCtx.psiblast.parseResult(jsValue)
              (result, (hsp: HSP) => views.html.psiblast.hit(hsp.asInstanceOf[PSIBlastHSP], result.db, wrapped))
            case _ => throw new IllegalArgumentException("tool has no hitlist") // TODO integrate Alignmnent Ctrl
          }
        case None => throw new IllegalStateException("no result found")
      }
      .map {
        case (result, view) =>
          if (data.end > result.num_hits || data.start > result.num_hits) {
            BadRequest
          } else {
            val hits = result.HSPS.slice(data.start, data.end).map(view)
            Ok(hits.mkString)
          }
      }
  }

  def dataTable(jobId: String): Action[AnyContent] = Action.async { implicit request =>
    val params = DTParam(
      request.getQueryString("draw").getOrElse("1").toInt,
      request.getQueryString("search[value]").getOrElse(""),
      request.getQueryString("start").getOrElse("0").toInt,
      request.getQueryString("length").getOrElse("100").toInt,
      request.getQueryString("order[0][column]").getOrElse("1").toInt,
      request.getQueryString("order[0][dir]").getOrElse("asc")
    )
    getResults(jobId)
      .run(resultsService)
      .flatMap {
        case Some(jsValue) =>
          getTool(jobId).run(resultsService).map {
            case HHBLITS =>
              (resultCtx.hhblits.parseResult(jsValue),
               getHitsByKeyWord[HHBlitsHSP](resultCtx.hhblits.parseResult(jsValue), params))
            case HHOMP =>
              (resultCtx.hhomp.parseResult(jsValue),
               getHitsByKeyWord[HHompHSP](resultCtx.hhomp.parseResult(jsValue), params))
            case HHPRED =>
              (resultCtx.hhpred.parseResult(jsValue),
               getHitsByKeyWord[HHPredHSP](resultCtx.hhpred.parseResult(jsValue), params))
            case HMMER =>
              (resultCtx.hmmer.parseResult(jsValue),
               getHitsByKeyWord[HmmerHSP](resultCtx.hmmer.parseResult(jsValue), params))
            case PSIBLAST =>
              (resultCtx.psiblast.parseResult(jsValue),
               getHitsByKeyWord[PSIBlastHSP](resultCtx.psiblast.parseResult(jsValue), params))
            case _ => throw new IllegalArgumentException("datatables not implemented for this tool")
          }
        case None => throw new IllegalStateException("no result found")
      }
      .map {
        case (result, hits) =>
          Ok(
            Json
              .toJson(Map("draw" -> params.draw, "recordsTotal" -> result.num_hits, "recordsFiltered" -> hits.length))
              .as[JsObject]
              .deepMerge(
                Json.obj(
                  "data" -> hits
                    .slice(params.displayStart, params.displayStart + params.pageLength)
                    .map(_.toDataTable(result.db))
                )
              )
          )
      }
  }

}
