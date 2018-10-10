package de.proteinevolution.results.services
import de.proteinevolution.models.ToolName
import io.circe.Json

trait HHService {

  // gets json and returns a tuple consisting of (a searchtoolresult , eta function from hsp => view)
  // erst fp to the max anschauen?

  protected def parseResult(tool: ToolName, json: Json) = {

    for {
      a <- json.hcursor
    } yield {}

  }

}
/*
 case HHBLITS =>
              (jsValue.as[HHBlitsResult],
               (hsp: HSP) => views.html.hhblits.hit(hsp.asInstanceOf[HHBlitsHSP], wrapped, jobId))
            case HHPRED =>
              (jsValue.as[HHPredResult],
               (hsp: HSP) => views.html.hhpred.hit(hsp.asInstanceOf[HHPredHSP], isColor, wrapped, jobId))
            case HHOMP =>
              (jsValue.as[HHompResult],
               (hsp: HSP) => views.html.hhomp.hit(hsp.asInstanceOf[HHompHSP], isColor, wrapped, jobId))
            case HMMER =>
              val result = jsValue.as[HmmerResult]
              (result, (hsp: HSP) => views.html.hmmer.hit(hsp.asInstanceOf[HmmerHSP], result.db, wrapped))
            case PSIBLAST =>
              val result = jsValue.as[PSIBlastResult].toOption
              (result.get, (hsp: HSP) => views.html.psiblast.hit(hsp.asInstanceOf[PSIBlastHSP], result.get.db, wrapped))
            case _ => throw new IllegalArgumentException("tool has no hitlist") // TODO integrate Alignmnent Ctrl
          }
        case None => throw new IllegalStateException("no result found")
 */
