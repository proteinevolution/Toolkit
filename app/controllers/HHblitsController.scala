package controllers

import javax.inject.Inject

import better.files._
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.Constants
import de.proteinevolution.tools.results.General.DTParam
import de.proteinevolution.tools.results.HHBlits
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.tools.results.HHBlits.{ HHBlitsHSP, HHBlitsResult }
import org.webjars.play.WebJarsUtil
import play.api.Logger
import play.api.libs.json.{ JsObject, Json }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }
import scala.sys.process._

class HHblitsController @Inject()(resultFiles: ResultFileAccessor,
                                  hhblits: HHBlits,
                                  webJarsUtil: WebJarsUtil,
                                  constants: Constants,
                                  cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with CommonController {

  /* gets the path to all scripts that are executed
   on the server (not executed on the grid engine) */

  private val serverScripts           = ConfigFactory.load().getString("serverScripts")
  private val templateAlignmentScript = (serverScripts + "/templateAlignmentHHblits.sh").toFile
  private val generateAlignmentScript = (serverScripts + "/generateAlignment.sh").toFile
  private val retrieveFullSeq         = (serverScripts + "/retrieveFullSeqHHblits.sh").toFile

  /**
   * returns 3D structure view for a given accession
   * in scop or mmcif
   * @param accession
   * @return 3D structure view
   */
  def show3DStructure(accession: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.jobs.resultpanels.structure(accession, webJarsUtil))
  }

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
   * Retrieves the full sequences of all hits with
   * an evalue below a threshold and writes the sequences
   * to a given filename within the current job folder
   * tp '@fileName'.fa
   *
   * Expects json sent by POST including:
   *
   * fileName: to which the full length sequences are written
   * evalue: seqs of all hits below this threshold
   * are retrieved from the DB
   * @param jobID
   * @return Https response
   */
  def evalFull(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json     = request.body.asJson.get
    val filename = (json \ "fileName").as[String]
    val eval     = (json \ "evalue").as[String]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
    } else {
      resultFiles.getResults(jobID).map {
        case None => NotFound
        case Some(jsValue) =>
          val result        = hhblits.parseResult(jsValue)
          val accessionsStr = getAccessionsEval(result, eval.toDouble)
          val db            = result.db
          Process(retrieveFullSeq.pathAsString,
                  (constants.jobPath + jobID).toFile.toJava,
                  "jobID"         -> jobID,
                  "accessionsStr" -> accessionsStr,
                  "filename"      -> filename,
                  "db"            -> db).run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }
      }
    }
  }

  /**
   * Retrieves the full sequences of all selected hits
   * in the result view and writes the sequences
   * to a given filename within the current job folder
   * to '@resultName'.fas
   *
   * Expects json sent by POST including:
   *
   * fileName: to which the full length sequences are written
   * checkboxes: an array which contains the numbers (in the HSP list)
   * of all hits that will be retrieved
   * @param jobID
   * @return Https response
   */
  def full(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json     = request.body.asJson.get
    val filename = (json \ "fileName").as[String]
    val numList  = (json \ "checkboxes").as[List[Int]]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
    } else {
      resultFiles.getResults(jobID).map {
        case None => NotFound
        case Some(jsValue) =>
          val result        = hhblits.parseResult(jsValue)
          val accessionsStr = getAccessions(result, numList)
          val db            = result.db
          Process(retrieveFullSeq.pathAsString,
                  (constants.jobPath + jobID).toFile.toJava,
                  "jobID"         -> jobID,
                  "accessionsStr" -> accessionsStr,
                  "filename"      -> filename,
                  "db"            -> db).run().exitValue() match {
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
    } else {
      resultFiles.getResults(jobID).map {
        case None => NotFound
        case Some(jsValue) =>
          val result     = hhblits.parseResult(jsValue)
          val numListStr = getNumListEval(result, eval.toDouble)
          Process(generateAlignmentScript.pathAsString,
                  (constants.jobPath + jobID).toFile.toJava,
                  "jobID"    -> jobID,
                  "filename" -> filename,
                  "numList"  -> numListStr).run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }
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
   * filters HSPS for hits below a given evalue threshold
   * and returns a string with the numbers whitespace speparated
   * @param result
   * @param eval
   * @return
   */
  def getNumListEval(result: HHBlitsResult, eval: Double): String = {
    val numList = result.HSPS.filter(_.info.evalue < eval).map { _.num }
    numList.mkString(" ")
  }

  /**
   * given an array of hit numbers this method
   * returns the corresponding accessions whitespace
   * separated as a string
   *
   * @param result
   * @param numList
   * @return string containing whitespace
   *         separated accessions
   */
  def getAccessions(result: HHBlitsResult, numList: Seq[Int]): String = {
    val fas = numList.map { num =>
      result.HSPS(num - 1).template.accession + " "
    }
    fas.mkString
  }

  /**
   * given an evalue threshold this method
   * returns the corresponding accessions whitespace
   * separated
   * @param eval
   * @param result
   * @return string containing whitespace
   *         separated accessions
   */
  def getAccessionsEval(result: HHBlitsResult, eval: Double): String = {
    val fas = result.HSPS.filter(_.info.evalue < eval).map { _.template.accession + " " }
    fas.mkString
  }

  /**
   * given dataTable specific paramters, this function
   * filters for eg. a specific column and returns the data
   * @param hits
   * @param params
   * @return
   */
  def getHitsByKeyWord(hits: HHBlitsResult, params: DTParam): List[HHBlitsHSP] = {
    if (params.sSearch.isEmpty) {
      hits.hitsOrderBy(params).slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
    } else {
      hits.hitsOrderBy(params).filter(_.description.contains(params.sSearch))
    }
  }

  /**
   * Retrieves hit rows (String containing Html)
   * for the alignment section in the result view
   * for a given range (start, end). Those can be either
   * wrapped or unwrapped
   *
   * Expects json sent by POST including:
   *
   * start: index of first HSP that is retrieved
   * end: index of last HSP that is retrieved
   * wrapped: Boolean true = wrapped, false = unwrapped
   *
   * @param jobID
   * @return Https response: HSP row(s) as String
   */
  def loadHits(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val start   = (json \ "start").as[Int]
    val end     = (json \ "end").as[Int]
    val wrapped = (json \ "wrapped").as[Boolean]
    resultFiles.getResults(jobID).map {
      case None => NotFound
      case Some(jsValue) =>
        val result = hhblits.parseResult(jsValue)
        if (end > result.num_hits || start > result.num_hits) {
          BadRequest
        } else {
          val hits =
            result.HSPS.slice(start, end).map { views.html.jobs.resultpanels.hhblits.hit(_, wrapped) }
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
        val result = hhblits.parseResult(jsValue)
        val hits   = getHitsByKeyWord(result, params)
        Ok(
          Json
            .toJson(Map("iTotalRecords" -> result.num_hits, "iTotalDisplayRecords" -> result.num_hits))
            .as[JsObject]
            .deepMerge(Json.obj("aaData" -> hits.map(_.toDataTable)))
        )
    }
  }
}