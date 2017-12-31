package controllers

import javax.inject.Inject

import better.files._
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.Constants
import de.proteinevolution.tools.results._
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.tools.results.HHPred.HHPredResult
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }
import scala.sys.process._

/**
 *
 * HHpred Controller process all requests
 * made from the HHpred result view
 */
class HHpredController @Inject()(resultFiles: ResultFileAccessor,
                                 hhpred: HHPred,
                                 constants: Constants,
                                 cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with CommonController {


  private val serverScripts           = ConfigFactory.load().getString("serverScripts")
  private val generateAlignmentScript = (serverScripts + "/generateAlignment.sh").toFile


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
        case None => BadRequest
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


}
