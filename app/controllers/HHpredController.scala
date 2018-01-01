package controllers

import javax.inject.Inject

import better.files._
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.Constants
import play.api.mvc._

import scala.concurrent.Future
import scala.sys.process._

/**
 *
 * HHpred Controller process all requests
 * made from the HHpred result view
 */
class HHpredController @Inject()(constants: Constants,
                                 cc: ControllerComponents)
    extends AbstractController(cc)
    with CommonController {


  private val serverScripts           = ConfigFactory.load().getString("serverScripts")
  private val generateAlignmentScript = (serverScripts + "/generateAlignment.sh").toFile


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

}
