package de.proteinevolution.results.controllers

import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.results.models.HHContext
import de.proteinevolution.results.results.Common
import de.proteinevolution.tel.env.Env
import javax.inject.Inject
import play.api.mvc.{ AbstractController, Action, AnyContent }

import scala.concurrent.ExecutionContext

class FileController @Inject()(ctx: HHContext, env: Env, constants: ConstantsV2)(implicit ec: ExecutionContext)
    extends AbstractController(ctx.controllerComponents) {

  def getStructureFile(filename: String): Action[AnyContent] = Action { implicit request =>
    val db = Common.identifyDatabase(filename.replaceAll("(.cif)|(.pdb)", ""))
    val filepath = db match {
      case "scop" =>
        env.get("SCOPE")
      case "ecod" =>
        env.get("ECOD")
      case "mmcif" =>
        env.get("CIF")
    }

    Ok.sendFile(new java.io.File(s"$filepath${constants.SEPARATOR}$filename")).as("application/octet-stream")

  }

}
