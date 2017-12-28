package controllers

import javax.inject.Inject

import better.files._
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.Constants
import de.proteinevolution.tools.results.General.DTParam
import de.proteinevolution.tools.results.Hmmer
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.tools.results.Hmmer.{ HmmerHSP, HmmerResult }
import play.api.libs.json.{ JsObject, Json }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }
import scala.sys.process.Process

class HmmerController @Inject()(resultFiles: ResultFileAccessor,
                                hmmer: Hmmer,
                                cc: ControllerComponents,
                                constants: Constants)(
    implicit val ec: ExecutionContext
) extends AbstractController(cc)
    with CommonController {
  /* gets the path to all scripts that are executed
     on the server (not executed on the grid engine) */
  private val serverScripts   = ConfigFactory.load().getString("serverScripts")
  private val retrieveFullSeq = (serverScripts + "/retrieveFullSeq.sh").toFile
  private val retrieveAlnEval = (serverScripts + "/retrieveAlnEval.sh").toFile

  /**
   * Retrieves the full sequences of all hits with
   * an evalue below a threshold and writes the sequences
   * to a given filename within the current job folder
   * to '@fileName'.fa
   *
   * Expects json sent by POST including:
   *
   * filename: to which the full length sequences are written
   * evalue: seqs of all hits below this threshold
   * are retrieved from the DB
   * @param jobID
   * @return Https response
   */
  def evalFull(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json     = request.body.asJson.get
    val filename = (json \ "filename").as[String]
    val eval     = (json \ "evalue").as[String]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
    } else {
      resultFiles.getResults(jobID).map {
        case None => NotFound
        case Some(jsValue) =>
          val result        = hmmer.parseResult(jsValue)
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
   * in the result view and saves the sequences
   * to a given filename within the current job folder
   * to '@fileName'.fa
   *
   * Expects json sent by POST including:
   *
   * filename: to which the full length sequences are written
   * checkboxes: an array which contains the numbers (in the HSP list)
   * of all hits that will be retrieved
   * @param jobID
   * @return Https response
   */
  def full(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json     = request.body.asJson.get
    val filename = (json \ "filename").as[String]
    val numList  = (json \ "checkboxes").as[List[Int]]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
    } else {
      resultFiles.getResults(jobID).map {
        case None => NotFound
        case Some(jsValue) =>
          val result        = hmmer.parseResult(jsValue)
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
   * given an array of hit numbers this method
   * returns the corresponding accessions whitespace
   * separated
   *
   * @param result
   * @param numList
   * @return string containing whitespace
   *         separated accessions
   */
  def getAccessions(result: HmmerResult, numList: Seq[Int]): String = {
    val fas = numList.map { num =>
      result.HSPS(num - 1).accession + " "
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
  def getAccessionsEval(result: HmmerResult, eval: Double): String = {
    val fas = result.HSPS.filter(_.evalue < eval).map { _.accession + " " }
    fas.mkString
  }

  /**
   * Retrieves the aligned sequences
   * (parsable alignment must be
   * provided in the result folder as JSON) of all hits with
   * an evalue below a threshold and returns the
   * sequences as a String
   *
   * Expects json sent by POST including:
   *
   * evalue: seqs of all hits below this threshold
   * are retrieved from the alignment
   *
   * @param jobID
   * @return aligned sequences as a String
   *         encapsulated in the response
   */
  def alnEval(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json     = request.body.asJson.get
    val filename = (json \ "filename").as[String]
    val eval     = (json \ "evalue").as[String]

    if (!retrieveAlnEval.isExecutable) {
      Future.successful(BadRequest)
    } else {

      resultFiles.getResults(jobID).map {
        case None => NotFound
        case Some(jsValue) =>
          val accessionsStr = getAlnEval(hmmer.parseResult(jsValue), eval.toDouble)
          // execute the script and pass parameters
          Process(retrieveAlnEval.pathAsString,
                  (constants.jobPath + jobID).toFile.toJava,
                  "accessionsStr" -> accessionsStr,
                  "filename"      -> filename,
                  "mode"          -> "count").run().exitValue() match {
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
   * saves returns the sequences as a String
   *
   * Expects json sent by POST including:
   *
   * checkboxes: an array which contains the numbers (in the HSP list)
   * of all hits that will be retrieved
   *
   * @param jobID
   * @return Https response containing the aligned sequences as String
   */
  def aln(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json     = request.body.asJson.get
    val numList  = (json \ "checkboxes").as[List[Int]].mkString("\n")
    val filename = (json \ "filename").as[String]

    if (!retrieveAlnEval.isExecutable) {
      Future.successful(BadRequest)
    } else {

      resultFiles.getResults(jobID).map {
        case None => NotFound
        case Some(_) =>
          val accessionsStr = numList
          // execute the script and pass parameters
          Process(retrieveAlnEval.pathAsString,
                  (constants.jobPath + jobID).toFile.toJava,
                  "accessionsStr" -> accessionsStr,
                  "filename"      -> filename,
                  "mode"          -> "sel").run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }
      }
    }
  }

  /**
   * filters all HSPS that are below
   * a given threshold from the PSIblast
   * result model and returns a count of hits
   * that pass the filter
   * @param result
   * @param eval
   * @return fasta as String
   */
  def getAlnEval(result: HmmerResult, eval: Double): String = {
    val fas = result.HSPS.filter(_.evalue < eval).map { hit =>
      result.alignment(hit.num - 1).accession + "\n"
    }
    fas.size.toString
  }

  /**
   * given dataTable specific paramters, this function
   * filters for eg. a specific column and returns the data
   * @param hits
   * @param params
   * @return
   */
  def getHitsByKeyWord(hits: HmmerResult, params: DTParam): List[HmmerHSP] = {
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
      case Some(jsValue) =>
        val result = hmmer.parseResult(jsValue)
        if (end > result.num_hits || start > result.num_hits) {
          BadRequest
        } else {
          val hits =
            result.HSPS.slice(start, end).map(views.html.jobs.resultpanels.hmmer.hit(_, result.db, wrapped))
          Ok(hits.mkString)
        }
      case None => BadRequest
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
        val result = hmmer.parseResult(jsValue)
        val hits   = getHitsByKeyWord(result, params)
        Ok(
          Json
            .toJson(Map("iTotalRecords" -> result.num_hits, "iTotalDisplayRecords" -> result.num_hits))
            .as[JsObject]
            .deepMerge(Json.obj("aaData" -> hits.map(_.toDataTable(result.db))))
        )
    }
  }
}
