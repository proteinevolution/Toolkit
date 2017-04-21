package controllers

import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import modules.CommonModule
import models.database.results._
import play.api.libs.json.JsArray
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.Future

/**
  * Created by drau on 18.04.17.
  */
class HmmerController @Inject() (hmmer: Hmmer, general: General) (val reactiveMongoApi : ReactiveMongoApi) extends Controller with CommonModule {

  def alnEval(jobID: String, eval: String): Action[AnyContent] = Action.async { implicit request =>
      getResult(jobID).map {
        case Some(jsValue) => Ok(getAlnEval(hmmer.parseResult(jsValue), eval.toDouble))
        case _=> NotFound
      }
  }


  def aln(jobID : String, numList : Seq[Int]): Action[AnyContent] = Action.async { implicit request =>
      getResult(jobID).map {
        case Some(jsValue) => Ok(getAln(general.parseAlignment((jsValue \ "alignment").as[JsArray]), numList))
        case _ => NotFound
      }
  }

  def getAlnEval(result : HmmerResult, eval : Double): String = {
    val fas = result.HSPS.map { hit =>
      if(hit.evalue < eval){
        ">" + result.alignment(hit.num -1).accession + "\n" + result.alignment(hit.num-1).seq + "\n"
      }
    }
    fas.mkString
  }

  def getAln(alignment : Alignment, list: Seq[Int]): String = {
    val fas = list.map { num =>
        ">" + alignment.alignment(num - 1).accession + "\n" + alignment.alignment(num -1 ).seq + "\n"
      }
    fas.mkString
  }

  def getHitsByKeyWord(jobID: String, params: DTParam) : Future[List[HmmerHSP]] = {
    if (params.sSearch.isEmpty) {
      getResult(jobID).map {
        case Some(result) => hmmer.parseResult(result).HSPS.slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
      }
    } else {
      ???
    }
  }
}
