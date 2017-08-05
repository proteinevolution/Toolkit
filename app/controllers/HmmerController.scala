package controllers

import javax.inject.Inject

import com.typesafe.config.ConfigFactory
import better.files._
import models.Constants

import scala.concurrent.ExecutionContext.Implicits.global
import models.database.results._
import modules.db.MongoStore
import play.api.libs.json.{ JsArray, JsObject, Json }
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.Future
import scala.sys.process.Process

/**
  * Created by drau on 18.04.17.
  */
class HmmerController @Inject()(hmmer: Hmmer,
                                general: General,
                                aln: Alignment,
                                constants: Constants)(mongoStore: MongoStore,
                                                                                val reactiveMongoApi: ReactiveMongoApi,
                                                      cc: ControllerComponents)
  extends AbstractController(cc)
    with Common {
  /* gets the path to all scripts that are executed
     on the server (not executed on the grid engine) */
  private val serverScripts   = ConfigFactory.load().getString("serverScripts")
  private val retrieveFullSeq = (serverScripts + "/retrieveFullSeq.sh").toFile

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
    val json    = request.body.asJson.get
    val filename  = (json \ "filename").as[String]
    val eval      = (json \ "evalue").as[String]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    } else {
      mongoStore.getResult(jobID).map {
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

        case _ => NotFound
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
    val json    = request.body.asJson.get
    val filename  = (json \ "filename").as[String]
    val numList = (json \ "checkboxes").as[List[Int]]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    } else {
      mongoStore.getResult(jobID).map {
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

        case _ => NotFound
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
    val json    = request.body.asJson.get
    val eval      = (json \ "evalue").as[String]
    mongoStore.getResult(jobID).map {
      case Some(jsValue) => Ok(getAlnEval(hmmer.parseResult(jsValue), eval.toDouble))
      case _             => NotFound
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
    val json    = request.body.asJson.get
    val numList = (json \ "checkboxes").as[List[Int]]
    mongoStore.getResult(jobID).map {
      case Some(jsValue) => Ok(getAln(aln.parseAlignment((jsValue \ "alignment").as[JsArray]), numList))
      case _             => NotFound
    }
  }

  /**
    * filters all HSPS that are below
    * a given threshold from the PSIblast
    * result model and returns a fasta
    * of the filtered hits
    * @param result
    * @param eval
    * @return fasta as String
    */

  def getAlnEval(result: HmmerResult, eval: Double): String = {
    val fas = result.HSPS.filter(_.evalue < eval).map { hit =>
      ">" + result.alignment(hit.num - 1).accession + "\n" + result.alignment(hit.num - 1).seq + "\n"
    }
    fas.mkString
  }
  /**
    * given an array of hit numbers this method
    * returns a fasta containing the corresponding
    * aligned hits
    *
    * @param alignment
    * @param numList
    * @return fasta as String
    */

  def getAln(alignment: AlignmentResult, numList: Seq[Int]): String = {
    val fas = numList.map { num =>
      ">" + alignment.alignment(num - 1).accession + "\n" + alignment.alignment(num - 1).seq + "\n"
    }
    fas.mkString
  }

  /**
    * given dataTable specific paramters, this function
    * filters for eg. a specific column and returns the data
    * @param jobID
    * @param params
    * @return
    */

  def getHitsByKeyWord(jobID: String, params: DTParam): Future[List[HmmerHSP]] = {
    if (params.sSearch.isEmpty) {
      mongoStore.getResult(jobID).map {
        case Some(result) =>
          hmmer
            .hitsOrderBy(params, hmmer.parseResult(result).HSPS)
            .slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
      }
    } else {
      ???
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
    val json      = request.body.asJson.get
    val start     = (json \ "start").as[Int]
    val end       = (json \ "end").as[Int]
    val wrapped       = (json \ "wrapped").as[Boolean]
    mongoStore.getResult(jobID).map {
      case Some(jsValue) =>
        val result = hmmer.parseResult(jsValue)
        if (end > result.num_hits || start > result.num_hits) {
          BadRequest
        } else {
          val hits = result.HSPS.slice(start, end).map(views.html.jobs.resultpanels.hmmer.hit(jobID, _, result.db, wrapped))
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

    val hits = getHitsByKeyWord(jobID, params)
    var db   = ""
    val total = mongoStore.getResult(jobID).map {
      case Some(jsValue) =>
        val result = hmmer.parseResult(jsValue)
        db = result.db
        result.num_hits

    }
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
