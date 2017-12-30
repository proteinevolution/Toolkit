package controllers

import javax.inject.Inject

import better.files._
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.Constants
import play.api.Logger
import play.api.mvc._

import scala.concurrent.Future
import scala.sys.process._

/**
 *
 * HHpred Controller process all requests
 * made from the HHpred result view
 */
class HHompController @Inject()(constants: Constants,
                                cc: ControllerComponents)
    extends AbstractController(cc)
    with CommonController {

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


}
