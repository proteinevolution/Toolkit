package de.proteinevolution.auth.controllers

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.users.{ User, UserConfig }
import javax.inject.{ Inject, Singleton }
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ValidationController @Inject()(userSessions: UserSessions, constants: ConstantsV2, cc: ControllerComponents)(
    implicit ec: ExecutionContext
) extends ToolkitController(cc) {

  def validateModellerKey(inputOpt: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    inputOpt match {
      case Some(input) =>
        userSessions.getUser.flatMap { user =>
          if (user.userConfig.hasMODELLERKey) {
            Future.successful(Ok(Json.obj("isValid" -> true)))
          } else if (input == constants.modellerKey) {
            userSessions
              .modifyUserWithCache(
                BSONDocument(User.IDDB -> user.userID),
                BSONDocument(
                  "$set" ->
                  BSONDocument(
                    s"${User.USERCONFIG}.${UserConfig.HASMODELLERKEY}" ->
                    true
                  )
                )
              )
              .map {
                case Some(_) =>
                  Ok(Json.obj("isValid" -> true))
                case None =>
                  BadRequest
              }
          } else {
            Future.successful(Ok(Json.obj("isValid" -> false)))
          }
        }
      case None =>
        Future.successful(BadRequest)
    }
  }

}
