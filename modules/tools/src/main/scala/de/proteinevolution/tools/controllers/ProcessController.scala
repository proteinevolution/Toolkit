package de.proteinevolution.tools.controllers
import javax.inject.{ Inject, Singleton }

import com.typesafe.config.ConfigFactory
import de.proteinevolution.tools.models.HHContext
import de.proteinevolution.tools.services.ToolNameGetService
import play.api.mvc.{ AbstractController, Action, AnyContent }
import better.files._
import de.proteinevolution.models.{ Constants, ToolNames }
import scala.sys.process.Process
import cats.implicits._
import cats.data.OptionT
import scala.concurrent.ExecutionContext

@Singleton
class ProcessController @Inject()(ctx: HHContext, toolFinder: ToolNameGetService, constants: Constants)(implicit ec: ExecutionContext)
    extends AbstractController(ctx.controllerComponents) {

  private val serverScripts = ConfigFactory.load().getString("serverScripts")

  def templateAlignment(jobID: String, accession: String): Action[AnyContent] = Action.async { implicit request =>
    val futureScript = toolFinder.getTool(jobID).map {
      case x if x == ToolNames.HHOMP   => (serverScripts + "/templateAlignmentHHomp.sh").toFile
      case x if x == ToolNames.HHBLITS => (serverScripts + "/templateAlignmentHHblits.sh").toFile
      case x if x == ToolNames.HHPRED  => (serverScripts + "/templateAlignment.sh").toFile
      case _                           => throw new IllegalStateException("tool either not found nor not supported")
    }
    (for {
      file <- OptionT.liftF(futureScript)
    } yield file).value.map {
      case Some(f) =>
        if (!f.isExecutable)
          BadRequest
        else {
          Process(f.pathAsString, (constants.jobPath + jobID).toFile.toJava, "jobID" -> jobID, "accession" -> accession)
            .run()
            .exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }
        }
      case None => BadRequest
    }
  }

}
