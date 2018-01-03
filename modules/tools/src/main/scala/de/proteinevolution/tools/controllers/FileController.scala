package de.proteinevolution.tools.controllers

import javax.inject.Inject

import de.proteinevolution.models.Constants
import de.proteinevolution.tel.env.Env
import de.proteinevolution.tools.models.HHContext
import de.proteinevolution.tools.results.Common
import play.api.mvc.{ AbstractController, Action, AnyContent }

import scala.concurrent.{ ExecutionContext, Future }

class FileController @Inject()(ctx: HHContext, env: Env, constants: Constants)(implicit ec: ExecutionContext)
    extends AbstractController(ctx.controllerComponents) {

  def getStructureFile(filename: String): Action[AnyContent] = Action.async { implicit request =>
    val db = Common.identifyDatabase(filename.replaceAll("(.cif)|(.pdb)", ""))
    val filepath = db match {
      case "scop" =>
        env.get("SCOPE")
      case "ecod" =>
        env.get("ECOD")
      case "mmcif" =>
        env.get("CIF")
    }
    Future.successful(Ok.sendFile(new java.io.File(s"$filepath${constants.SEPARATOR}$filename")).as("text/plain"))
  }

}
