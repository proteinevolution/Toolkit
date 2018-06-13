package de.proteinevolution.results.controllers

import de.proteinevolution.models.ToolName._
import de.proteinevolution.results.models.{ HHContext, ResultContext }
import de.proteinevolution.results.results.General.DTParam
import de.proteinevolution.results.results.HHBlits.HHBlitsHSP
import de.proteinevolution.results.results.HHPred.HHPredHSP
import de.proteinevolution.results.results.HHomp.HHompHSP
import de.proteinevolution.results.results.Hmmer.HmmerHSP
import de.proteinevolution.results.results.PSIBlast.PSIBlastHSP
import de.proteinevolution.results.results.{ HSP, SearchResult }
import de.proteinevolution.results.services.{ DTService, KleisliProvider }
import javax.inject.{ Inject, Singleton }
import play.api.libs.json.{ JsObject, Json }
import play.api.mvc.{ AbstractController, Action, AnyContent }

import scala.concurrent.ExecutionContext

@Singleton
class HHController @Inject()(ctx: HHContext,
                             resultCtx: ResultContext,
                             kleisliProvider: KleisliProvider,
                             dtService: DTService)(
    implicit ec: ExecutionContext
) extends AbstractController(ctx.controllerComponents) {

  def loadHits(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val start   = (json \ "start").as[Int]
    val end     = (json \ "end").as[Int]
    val wrapped = (json \ "wrapped").as[Boolean]
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
                val isColor = (json \ "isColor").as[Boolean]
                (resultCtx.hhpred.parseResult(jsValue).asInstanceOf[SearchResult[HSP]],
                 (hsp: HSP) => views.html.hhpred.hit(hsp.asInstanceOf[HHPredHSP], isColor, wrapped, jobID))
              case HHOMP =>
                val isColor = (json \ "isColor").as[Boolean]
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
          if (end > result.num_hits || start > result.num_hits) {
            BadRequest
          } else {
            val hits = result.HSPS.slice(start, end).map(view)
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
