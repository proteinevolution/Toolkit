/**
 *
 * PSIblast Controller process all requests
 * made from the PSIblast result view
 *
 */
package controllers

import javax.inject.Inject

import better.files._
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.Constants
import de.proteinevolution.tools.results.PSIBlast
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.tools.results.PSIBlast.PSIBlastResult
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }
import scala.sys.process._

class PSIBlastController @Inject()(resultFiles: ResultFileAccessor,
                                   psiblast: PSIBlast,
                                   constants: Constants,
                                   cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with CommonController {

  /* gets the path to all scripts that are executed
     on the server (not executed on the grid engine) */
  private val serverScripts   = ConfigFactory.load().getString("serverScripts")
  private val retrieveFullSeq = (serverScripts + "/retrieveFullSeq.sh").toFile
  private val retrieveAlnEval = (serverScripts + "/retrieveAlnEval.sh").toFile

  /**
   * Retrieves the full sequences of all hits with
   * an evalue below a threshold and saves the sequences
   * to a given filename within the current job folder
   * to '@fileName'.fa
   * Expects json sent by POST including:
   *
   * filename: to which the full
   * length sequences are written
   * evalue: seqs of all hits below this threshold
   * are retrieved from the DB
   * @param jobID
   * @return Https response
   */
  def evalFull(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    // retrieve parameters from the request
    val json     = request.body.asJson.get
    val filename = (json \ "filename").as[String]
    val eval     = (json \ "evalue").as[String]
    // check if the retrieve script is executable
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
    } else {
      resultFiles.getResults(jobID).map {
        case None => NotFound
        case Some(jsValue) =>
          val result        = psiblast.parseResult(jsValue)
          val accessionsStr = getAccessionsEval(result, eval.toDouble)
          val db            = result.db
          // execute the script and pass parameters
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
   * Expects json sent by POST including:
   *
   * filename: to which the full
   * length sequences are written
   * checkboxes: an array which contains the numbers (in the HSP list)
   * of all hits that will be retrieved
   * @param jobID
   * @return Https response
   */
  def full(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json     = request.body.asJson.get
    val numList  = (json \ "checkboxes").as[List[Int]]
    val filename = (json \ "filename").as[String]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
    } else {
      resultFiles.getResults(jobID).map {
        case None => NotFound
        case Some(jsValue) =>
          val result        = psiblast.parseResult(jsValue)
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
   * given an array of hit numbers this method
   * returns the corresponding accessions whitespace
   * separated
   *
   * @param result
   * @param numList
   * @return string containing whitespace
   *         separated accessions
   */
  def getAccessions(result: PSIBlastResult, numList: Seq[Int]): String = {
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
  def getAccessionsEval(result: PSIBlastResult, eval: Double): String = {
    val fas = result.HSPS.filter(_.evalue < eval).map { _.accession + " " }
    fas.mkString
  }

}
