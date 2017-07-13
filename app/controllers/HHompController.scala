package controllers

import javax.inject.Inject

import better.files._
import com.typesafe.config.ConfigFactory
import models.Constants
import models.database.results.{HHomp, HHompHSP, HHompResult}
import modules.db.MongoStore
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process._

/**
  * Created by drau on 01.03.17.
  *
  * HHpred Controller process all requests
  * made from the HHpred result view
  */
class HHompController @Inject()(hhomp: HHomp, mongoStore: MongoStore, val reactiveMongoApi: ReactiveMongoApi, constants: Constants)(
    webJarAssets: WebJarAssets
) extends Controller
    with Common {

  /* gets the path to all scripts that are executed
     on the server (not executed on the grid eninge) */
  private val serverScripts           = ConfigFactory.load().getString("serverScripts")
  private val templateAlignmentScript = (serverScripts + "/templateAlignmentHHomp.sh").toFile


  /**
    * Retrieves the template alignment for a given
    * accession, therefore it runs a script on the server
    * (now grid engine) and writes it to the current job folder
    * to 'accession'.fas
    *
    * @param jobID
    * @param accession
    * @return Http response
    */
  def retrieveTemplateAlignment(jobID: String, accession: String): Action[AnyContent] = Action.async {
    if (jobID.isEmpty || accession.isEmpty) {
      Logger.info("either job or accession is empty")
    }
    if (!templateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${templateAlignmentScript.name} is not executable.")
    } else {
      Future.successful {
        Process(templateAlignmentScript.pathAsString,
          (constants.jobPath + jobID).toFile.toJava,
          "jobID"     -> jobID,
          "accession" -> accession).run().exitValue() match {
          case 0 => Ok
          case _ => BadRequest
        }
      }
    }
  }



  /**
    * given dataTable specific paramters, this function
    * filters for eg. a specific column and returns the data
    *
    * @param jobID
    * @param params
    * @return
    */

  def getHitsByKeyWord(jobID: String, params: DTParam): Future[List[HHompHSP]] = {
    if (params.sSearch.isEmpty) {
      mongoStore.getResult(jobID).map {
        case Some(result) =>
          hhomp
            .hitsOrderBy(params, hhomp.parseResult(result).HSPS)
            .slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
      }
    } else {
      ???
    }
    //case false => (for (s <- getHits if (title.startsWith(params.sSearch))) yield (s)).list
  }

  /**
    * Retrieves hit rows (String containing Html)
    * for the alignment section in the result view
    * for a given range (start, end). Those can be either
    * wrapped or unwrapped, colored or uncolored
    *
    * Expects json sent by POST including:
    *
    * start: index of first HSP that is retrieved
    * end: index of last HSP that is retrieved
    * wrapped: Boolean true = wrapped, false = unwrapped
    * isColored: Boolean true = colored, false = uncolored
    *
    * @param jobID
    * @return Https response: HSP row(s) as String
    */

  def loadHits(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json      = request.body.asJson.get
    val start     = (json \ "start").as[Int]
    val end       = (json \ "end").as[Int]
    val isColor   = (json \ "isColor").as[Boolean]
    val wrapped   = (json \ "wrapped").as[Boolean]
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result = hhomp.parseResult(jsValue)
          if (end > result.num_hits || start > result.num_hits) {
            BadRequest
          } else {
            val hits = result.HSPS.slice(start, end).map(views.html.jobs.resultpanels.hhomp.hit(jobID, _, isColor, wrapped))
            Ok(hits.mkString)
          }
      }
  }
  /**
    * this method fetches the data for the PSIblast hitlist
    * datatable
    *
    * @param jobID
    * @return
    */
  def dataTable(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    var db = ""
    val total = mongoStore.getResult(jobID).map {
      case Some(jsValue) =>
        val result = hhomp.parseResult(jsValue)
        db = result.db
        result.num_hits

    }
    val params = DTParam(
      request.getQueryString("sSearch").getOrElse(""),
      request.getQueryString("iDisplayStart").getOrElse("0").toInt,
      request.getQueryString("iDisplayLength").getOrElse("100").toInt,
      request.getQueryString("iSortCol_0").getOrElse("1").toInt,
      request.getQueryString("sSortDir_0").getOrElse("asc")
    )

    val hits = getHitsByKeyWord(jobID, params)

    hits.flatMap { list =>
      total.map { total_ =>
        Ok(
          Json
            .toJson(Map("iTotalRecords" -> total_, "iTotalDisplayRecords" -> total_))
            .as[JsObject]
            .deepMerge(Json.obj("aaData" -> list.map(_.toDataTable(db))))
        )
      }
    }
  }
}
