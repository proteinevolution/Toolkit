package models.auth

import models.database.User
import play.api.libs.json.Json

/**
  * Created by astephens on 26.05.16.
  */
trait JSONTemplate {
  /**
    * Creates a simplified JSON Object from a User Object
    * @param user
    * @return
    */
  def userToJSON (user : User) = {
    Json.obj("name_login" -> user.name_login,
             "name_last"  -> user.name_last,
             "name_first" -> user.name_first)
  }

  /**
    * Creates a JSON Object from an Auth Action Object
    * @param authAction
    * @return
    */
  def authActionToJSON (authAction: AuthAction) = {
    authAction.user_o match {
      case Some(user) =>
        Json.obj("message" -> authAction.message,
                 "successful" -> authAction.success,
                 "user"    -> userToJSON(user))
      case None =>
        Json.obj("message" -> authAction.message,
                 "successful" -> authAction.success)
    }
  }
}
