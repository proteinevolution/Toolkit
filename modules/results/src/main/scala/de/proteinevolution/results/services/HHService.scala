package de.proteinevolution.results.services

import cats.data.EitherT
import cats.implicits._
import de.proteinevolution.models.ToolName
import de.proteinevolution.models.ToolName._
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.results.models.ResultsForm
import de.proteinevolution.results.results.General.DTParam
import de.proteinevolution.results.results._
import de.proteinevolution.results.services.ResultsRepository.ResultsService
import io.circe.DecodingFailure
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.twirl.api.HtmlFormat

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class HHService @Inject()(
    toolFinder: ToolNameGetService,
    resultFiles: ResultFileAccessor
)(implicit ec: ExecutionContext)
    extends ResultsRepository
    with DTService {

  private val logger = Logger(this.getClass)

  private val resultsService = ResultsService(toolFinder, resultFiles)

  def loadHits(jobId: String, form: ResultsForm): EitherT[Future, DecodingFailure, List[HtmlFormat.Appendable]] = {
    EitherT((for {
      json <- getResults(jobId).run(resultsService)
      tool <- getTool(jobId).run(resultsService)
    } yield {
      (json, tool)
    }).map {
      case (Some(json), tool) => parseResult(tool, json)
      case _ =>
        val error = "parsing result json failed."
        logger.error(error)
        Left(DecodingFailure(error, Nil))
    }).subflatMap {
      case (result, tool) =>
        if (form.end > result.num_hits || form.start > result.num_hits) {
          Left(DecodingFailure("", Nil))
        } else {
          Right(result.HSPS.slice(form.start, form.end).map(hsp => createView(jobId, tool, hsp, form, result)))
        }
    }
  }

  def dataTable(jobId: String, params: DTParam): EitherT[Future, DecodingFailure, (List[HSP], SearchResult[HSP])] = {
    EitherT((for {
      json <- getResults(jobId).run(resultsService)
      tool <- getTool(jobId).run(resultsService)
    } yield {
      (json, tool)
    }).map {
      case (Some(json), tool) => parseResult(tool, json)
      case _ =>
        val error = "parsing result json failed."
        logger.error(error)
        Left(DecodingFailure(error, Nil))
    }).map {
      case (result, tool) => (generateDTQuery(tool, params, result), result)
    }
  }

  private[this] def generateDTQuery(tool: ToolName, params: DTParam, result: SearchResult[_]): List[HSP] = {
    tool match {
      case HHBLITS  => getHitsByKeyWord[HHBlitsHSP](result.asInstanceOf[HHBlitsResult], params)
      case HHOMP    => getHitsByKeyWord[HHompHSP](result.asInstanceOf[HHompResult], params)
      case HHPRED   => getHitsByKeyWord[HHPredHSP](result.asInstanceOf[HHPredResult], params)
      case HMMER    => getHitsByKeyWord[HmmerHSP](result.asInstanceOf[HmmerResult], params)
      case PSIBLAST => getHitsByKeyWord[PSIBlastHSP](result.asInstanceOf[PSIBlastResult], params)
      case _        => throw new IllegalStateException("no search feature available for this tool")
    }
  }

  private[this] def createView(
      jobId: String,
      tool: ToolName,
      hsp: HSP,
      form: ResultsForm,
      result: SearchResult[HSP]
  ): HtmlFormat.Appendable = {
    val wrapped = form.wrapped.getOrElse(false)
    val isColor = form.isColor.getOrElse(false)
    tool match {
      case HHBLITS  => views.html.hhblits.hit(hsp.asInstanceOf[HHBlitsHSP], wrapped, jobId)
      case HHPRED   => views.html.hhpred.hit(hsp.asInstanceOf[HHPredHSP], isColor, wrapped, jobId)
      case HHOMP    => views.html.hhomp.hit(hsp.asInstanceOf[HHompHSP], isColor, wrapped, jobId)
      case HMMER    => views.html.hmmer.hit(hsp.asInstanceOf[HmmerHSP], result.db, wrapped)
      case PSIBLAST => views.html.psiblast.hit(hsp.asInstanceOf[PSIBlastHSP], result.db, wrapped)
      case _        => throw new IllegalArgumentException("tool has no hitlist")
    }
  }

}
