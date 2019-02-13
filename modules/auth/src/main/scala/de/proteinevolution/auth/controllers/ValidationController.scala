package de.proteinevolution.auth.controllers

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.common.models.database.users.{ User, UserConfig }
import io.circe.{ Json, JsonObject }
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext

@Singleton
class ValidationController @Inject()(userSessions: UserSessions, constants: ConstantsV2, cc: ControllerComponents)(
    implicit ec: ExecutionContext
) extends ToolkitController(cc) {

  final private[this] def isValid(v: Boolean): Json = JsonObject("isValid" -> Json.fromBoolean(v)).asJson

  def validateModellerKey(inputOpt: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    inputOpt match {
      case Some(input) =>
        userSessions.getUser.flatMap { user =>
          if (user.userConfig.hasMODELLERKey) {
            fuccess(Ok(isValid(true)))
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
                  Ok(isValid(true))
                case None =>
                  BadRequest
              }
          } else {
            fuccess(Ok(isValid(false)))
          }
        }
      case None =>
        fuccess(BadRequest)
    }
  }

}
