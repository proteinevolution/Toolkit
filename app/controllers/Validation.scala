package controllers

import org.mindrot.jbcrypt.BCrypt
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.libs.json.Json
import scala.concurrent.Future

/**
  * This controller is useful for validation in the frontend BEFORE submission
  * when we validate against hidden values and database entries
  *
  */

class Validation extends Controller {

  // TODO implement me

  // Bcrypt, Salt 12

  val modeller_key = "$2a$12$ySkBGrEDxCDLf5aNVl2fc.8VnDGf9Ve003WbV8Lw8mfnJ.Lae49G."

  def validateModellerKey(input: String) :  Action[AnyContent] = Action.async { implicit request =>

    input match {
      case x if BCrypt.checkpw(x, modeller_key) => Future.successful(Ok(Json.toJson("valid")))
      case x if !BCrypt.checkpw(x, modeller_key) => Future.successful(Ok(Json.toJson("invalid")))
      case _ => Future.successful(Ok(Json.toJson("undefined")))

    }

  }

}
