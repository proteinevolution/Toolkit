package controllers

import javax.inject.Inject
import java.nio.file.attribute.PosixFilePermission

import com.typesafe.config.ConfigFactory
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process._
import better.files._
import models.Constants
import models.database.results.{ HHPred, HHPredHSP, HHPredResult }
import modules.db.MongoStore
import play.modules.reactivemongo.ReactiveMongoApi
import play.api.libs.json.{ JsArray, JsObject, Json }

/**
  * Created by drau on 01.03.17.
  *
  * HHpred Controller process all requests
  * made from the HHpred result view
  */
class HHpredController @Inject()(hhpred: HHPred,
                                 mongoStore: MongoStore,
                                 val reactiveMongoApi: ReactiveMongoApi,
                                 constants: Constants)(webJarAssets: WebJarAssets, cc: ControllerComponents)
    extends AbstractController(cc)
    with Common {

  /* gets the path to all scripts that are executed
     on the server (not executed on the grid eninge) */
  private val serverScripts           = ConfigFactory.load().getString("serverScripts")
  private val templateAlignmentScript = (serverScripts + "/templateAlignment.sh").toFile
  private val generateAlignmentScript = (serverScripts + "/generateAlignment.sh").toFile

  /**
    * returns 3D structure view for a given accession
    * in scop or mmcif
    * @param accession
    * @return 3D structure view
    */
  def show3DStructure(accession: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.jobs.resultpanels.structure(accession, webJarAssets))
  }

  /**
    * Retrieves the template alignment for a given
    * accession, for this it runs a script on the server
    * (now grid engine) and writes it to the current job folder
    * to 'accession'.fas
    *
    * @param jobID
    * @param accession
    * @return Http response
    */
  def retrieveTemplateAlignment(jobID: String, accession: String): Action[AnyContent] = Action.async {
    implicit request =>
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
    * Retrieves the aligned sequences
    * (parsable alignment must be
    * provided in the result folder as JSON) of all hits with
    * an evalue below a threshold and writes the sequences to the
    * current job folder to '@resultName'.fa
    * Expects json sent by POST including:
    *
    * fileName: to which the aligned sequences are written
    * evalue: seqs of all hits below this threshold
    * are retrieved from the alignment
    *
    * @param jobID
    * @return
    */
  def alnEval(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json     = request.body.asJson.get
    val filename = (json \ "fileName").as[String]
    val eval     = (json \ "evalue").as[String]
    if (!generateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${generateAlignmentScript.name} is not executable.")
    } else {
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result     = hhpred.parseResult(jsValue)
          val numListStr = getNumListEval(result, eval.toDouble)
          Process(generateAlignmentScript.pathAsString,
                  (constants.jobPath + jobID).toFile.toJava,
                  "jobID"    -> jobID,
                  "filename" -> filename,
                  "numList"  -> numListStr).run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }

        case _ => NotFound
      }
    }
  }

  /**
    * Retrieves the aligned sequences (parsable alignment
    * must be provided in the result folder as JSON)
    * of all selected hits in the result view and
    * writes the sequences to the
    * current job folder to '@resultName'.fa
    *
    * Expects json sent by POST including:
    *
    * fileName: to which the aligned sequences are written
    * checkboxes: an array which contains the numbers (in the HSP list)
    * of all hits that will be retrieved
    *
    * @param jobID
    * @return
    */
  def aln(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json     = request.body.asJson.get
    val filename = (json \ "fileName").as[String]
    val numList  = (json \ "checkboxes").as[List[Int]]
    if (!generateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${generateAlignmentScript.name} is not executable.")
    } else {
      val numListStr = numList.mkString(" ")
      Process(generateAlignmentScript.pathAsString,
              (constants.jobPath + jobID).toFile.toJava,
              "jobID"    -> jobID,
              "filename" -> filename,
              "numList"  -> numListStr).run().exitValue() match {
        case 0 => Future.successful(Ok)
        case _ => Future.successful(BadRequest)
      }
    }
  }

  /**
    * filters HSPS for hits below a given threshold
    * and returns a string with the numbers of the filtered hits
    * whitespace separated
    * @param result
    * @param eval
    * @return
    */
  def getNumListEval(result: HHPredResult, eval: Double): String = {
    val numList = result.HSPS.filter(_.info.evalue < eval).map { _.num }
    numList.mkString(" ")
  }

  /**
    * given dataTable specific paramters, this function
    * filters for eg. a specific column and returns the data
    * @param jobID
    * @param params
    * @return
    */
  def getHitsByKeyWord(jobID: String, params: DTParam): Future[List[HHPredHSP]] = {
    if (params.sSearch.isEmpty) {
      mongoStore.getResult(jobID).map {
        case Some(result) =>
          hhpred
            .hitsOrderBy(params, hhpred.parseResult(result).HSPS)
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
    val json    = request.body.asJson.get
    val start   = (json \ "start").as[Int]
    val end     = (json \ "end").as[Int]
    val isColor = (json \ "isColor").as[Boolean]
    val wrapped = (json \ "wrapped").as[Boolean]
    mongoStore.getResult(jobID).map {
      case Some(jsValue) =>
        val result = hhpred.parseResult(jsValue)
        if (end > result.num_hits || start > result.num_hits) {
          BadRequest
        } else {
          val hits =
            result.HSPS.slice(start, end).map(views.html.jobs.resultpanels.hhpred.hit(jobID, _, isColor, wrapped))
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
        val result = hhpred.parseResult(jsValue)
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
