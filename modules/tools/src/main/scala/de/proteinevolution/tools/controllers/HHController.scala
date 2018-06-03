package de.proteinevolution.tools.controllers

import javax.inject.{ Inject, Singleton }
import de.proteinevolution.models.ToolName
import de.proteinevolution.tools.models.{ HHContext, ResultContext, ResultForm }
import de.proteinevolution.tools.results.General.DTParam
import de.proteinevolution.tools.results.HHBlits.HHBlitsHSP
import de.proteinevolution.tools.results.HHPred.HHPredHSP
import de.proteinevolution.tools.results.HHomp.HHompHSP
import de.proteinevolution.tools.results.Hmmer.HmmerHSP
import de.proteinevolution.tools.results.PSIBlast.PSIBlastHSP
import de.proteinevolution.tools.results.{ HSP, SearchResult }
import de.proteinevolution.tools.services.{ DTService, KleisliProvider }
import play.api.mvc.{ AbstractController, Action, AnyContent }
import ToolName._
import play.api.libs.circe.Circe
import io.circe.syntax._

import scala.concurrent.ExecutionContext

@Singleton
class HHController @Inject()(ctx: HHContext,
                             resultCtx: ResultContext,
                             kleisliProvider: KleisliProvider,
                             dtService: DTService)(
    implicit ec: ExecutionContext
) extends AbstractController(ctx.controllerComponents)
    with Circe {

  def loadHits(jobID: String): Action[ResultForm] = Action(circe.json[ResultForm]).async { implicit request =>
    val data    = request.body
    val wrapped = data.wrapped.getOrElse(false)
    val isColor = data.isColor.getOrElse(false)
    kleisliProvider
      .resK(jobID)
      .flatMap {
        case Some(jsValue) =>
          kleisliProvider
            .toolK(jobID)
            .map {
              case HHBLITS =>
                (resultCtx.hhblits.parseResult(jsValue).asInstanceOf[SearchResult[HSP]],
                 (hsp: HSP) => views.html.hhblits.hit(hsp.asInstanceOf[HHBlitsHSP], wrapped, jobID))
              case HHPRED =>
                (resultCtx.hhpred.parseResult(jsValue).asInstanceOf[SearchResult[HSP]],
                 (hsp: HSP) => views.html.hhpred.hit(hsp.asInstanceOf[HHPredHSP], isColor, wrapped, jobID))
              case HHOMP =>
                (resultCtx.hhomp.parseResult(jsValue).asInstanceOf[SearchResult[HSP]],
                 (hsp: HSP) => views.html.hhomp.hit(hsp.asInstanceOf[HHompHSP], isColor, wrapped, jobID))
              case HMMER =>
                val result = resultCtx.hmmer.parseResult(jsValue).asInstanceOf[SearchResult[HSP]]
                (result, (hsp: HSP) => views.html.hmmer.hit(hsp.asInstanceOf[HmmerHSP], result.db, wrapped))
              case PSIBLAST =>
                val result = resultCtx.psiblast.parseResult(jsValue).asInstanceOf[SearchResult[HSP]]
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

  def dataTable(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val params = DTParam(
      request.getQueryString("draw").getOrElse("1").toInt,
      request.getQueryString("search[value]").getOrElse(""),
      request.getQueryString("start").getOrElse("0").toInt,
      request.getQueryString("length").getOrElse("100").toInt,
      request.getQueryString("order[0][column]").getOrElse("1").toInt,
      request.getQueryString("order[0][dir]").getOrElse("asc")
    )
    kleisliProvider
      .resK(jobID)
      .flatMap {
        case Some(jsValue) =>
          kleisliProvider
            .toolK(jobID)
            .map {
              case HHBLITS =>
                (resultCtx.hhblits.parseResult(jsValue).asInstanceOf[SearchResult[HSP]],
                 dtService.getHitsByKeyWord[HHBlitsHSP](resultCtx.hhblits.parseResult(jsValue), params))
              case HHOMP =>
                (resultCtx.hhomp.parseResult(jsValue).asInstanceOf[SearchResult[HSP]],
                 dtService.getHitsByKeyWord[HHompHSP](resultCtx.hhomp.parseResult(jsValue), params))
              case HHPRED =>
                (resultCtx.hhpred.parseResult(jsValue).asInstanceOf[SearchResult[HSP]],
                 dtService.getHitsByKeyWord[HHPredHSP](resultCtx.hhpred.parseResult(jsValue), params))
              case HMMER =>
                (resultCtx.hmmer.parseResult(jsValue).asInstanceOf[SearchResult[HSP]],
                 dtService.getHitsByKeyWord[HmmerHSP](resultCtx.hmmer.parseResult(jsValue), params))
              case PSIBLAST =>
                (resultCtx.psiblast.parseResult(jsValue).asInstanceOf[SearchResult[HSP]],
                 dtService.getHitsByKeyWord[PSIBlastHSP](resultCtx.psiblast.parseResult(jsValue), params))
              case _ => throw new IllegalArgumentException("datatables not implemented for this tool")
            }
        case None => throw new IllegalStateException("no result found")
      }
      .map {
        case (result, hits) =>
          Ok(
            Map("draw" -> params.draw, "recordsTotal" -> result.num_hits, "recordsFiltered" -> hits.length).asJson
              .deepMerge(
                Map(
                  "data" -> hits
                    .slice(params.displayStart, params.displayStart + params.pageLength)
                    .map(_.toDataTable(result.db))
                ).asJson
              )
          )
      }
  }

}
