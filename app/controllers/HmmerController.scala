package controllers

import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import modules.CommonModule
import models.database.results._
import play.api.libs.json.{JsArray, JsObject, Json}
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
    val fas = result.HSPS.filter(_.evalue < eval).map { hit =>
        ">" + result.alignment(hit.num -1).accession + "\n" + result.alignment(hit.num-1).seq + "\n"
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
        case Some(result) => hmmer.hitsOrderBy(params, hmmer.parseResult(result).HSPS).slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
      }
    } else {
      ???
    }
  }

  def loadHits(jobID: String, start: Int, end: Int): Action[AnyContent] = Action.async { implicit request =>
    getResult(jobID).map {
      case Some(jsValue) => {
        val result = hmmer.parseResult(jsValue)
        if(end > result.num_hits || start > result.num_hits ) {
          BadRequest
        }else {
          val hits = result.HSPS.slice(start, end).map(views.html.jobs.resultpanels.hmmer.hit(jobID, _, result.db))
          Ok(hits.mkString)
        }
      }
    }
  }

  /**
    * DataTables for job results
    */

  def dataTable(jobID : String) : Action[AnyContent] = Action.async { implicit request =>
    val params = DTParam(
      request.getQueryString("sSearch").getOrElse(""),
      request.getQueryString("iDisplayStart").getOrElse("0").toInt,
      request.getQueryString("iDisplayLength").getOrElse("100").toInt,
      request.getQueryString("iSortCol_0").getOrElse("1").toInt,
      request.getQueryString("sSortDir_0").getOrElse("asc"))

    val hits = getHitsByKeyWord(jobID, params)
    var db = ""
    val total = getResult(jobID).map {
      case Some(jsValue) => {
        val result = hmmer.parseResult(jsValue)
        db = result.db
        result.num_hits
      }
    }
    hits.flatMap { list =>
      total.map { total_ =>
        Ok(Json.toJson(Map("iTotalRecords" -> total_, "iTotalDisplayRecords" -> total_))
          .as[JsObject].deepMerge(Json.obj("aaData" -> list.map(_.toDataTable(db)))))
      }
    }
  }
}
