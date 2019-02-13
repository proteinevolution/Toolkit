package de.proteinevolution.results.controllers

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.results.models.HHContext
import de.proteinevolution.results.results.Common
import de.proteinevolution.tel.env.Env
import javax.inject.Inject
import play.api.http.ContentTypes
import play.api.mvc.{ AbstractController, Action, AnyContent }

import scala.concurrent.ExecutionContext

class FileController @Inject()(
    ctx: HHContext,
    env: Env,
    constants: ConstantsV2,
    userSessions: UserSessions
)(implicit ec: ExecutionContext)
    extends AbstractController(ctx.controllerComponents)
    with ContentTypes {

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
    Ok.sendFile(new java.io.File(s"$filepath${constants.SEPARATOR}$filename")).as(BINARY)
  }

  def file(filename: String, jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      val file = new java.io.File(
        s"${constants.jobPath}${constants.SEPARATOR}$jobID${constants.SEPARATOR}results${constants.SEPARATOR}$filename"
      )
      if (file.exists) {
        Ok.sendFile(file)
          .withSession(userSessions.sessionCookie(request, user.sessionID.get))
          .as(TEXT) // text/plain in order to open the file in a new browser tab
      } else {
        NoContent
      }
    }
  }

}
