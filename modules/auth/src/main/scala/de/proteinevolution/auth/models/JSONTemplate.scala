/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.auth.models

import de.proteinevolution.user.User
import io.circe.Json
import io.circe.syntax._

trait JSONTemplate {

  final val SHOWPASSWORDRESETVIEW = "showPasswordResetView"

  def userToJSON(user: User): Json = {
    Json.obj("nameLogin" -> Json.fromString(user.getUserData.nameLogin))
  }

  def authMessage(
      messageKey: String,
      messageArguments: List[String] = List(),
      success: Boolean = false,
      userOption: Option[User] = None
  ): Json = {
    Json.obj(
      "messageKey"      -> Json.fromString(messageKey),
      "messageArguments" -> messageArguments.asJson,
      "successful"      -> Json.fromBoolean(success),
      "user"            -> userOption.map(user => user.userData.asJson).getOrElse(Json.Null)
    )
  }

  def loggedIn(user: User): Json = {
    authMessage(
      "loginSuccess",
      messageArguments = List(user.getUserData.nameLogin),
      success = true,
      userOption = Some(user)
    )
  }

  def signedUp(): Json = {
    authMessage("signedUp", success = true)
  }

  def loggedOut(): Json = {
    authMessage("loggedOut", success = true)
  }

  def loginError(): Json = {
    authMessage("loginError")
  }

  def accountNameUsed(): Json = {
    authMessage("accountNameUsed")
  }

  def accountEmailUsed(): Json = {
    authMessage("accountEmailUsed")
  }

  def accountError(): Json = {
    authMessage("accountError")
  }

  def loginIncorrect(): Json = {
    authMessage("loginIncorrect")
  }

  def mustAcceptToS(): Json = {
    authMessage("mustAcceptToS")
  }

  def mustVerify(): Json = {
    authMessage("mustVerify")
  }

  def alreadyLoggedIn(): Json = {
    authMessage("alreadyLoggedIn")
  }

  def passwordMismatch(): Json = {
    authMessage("passwordMismatch")
  }

  def passwordWrong(): Json = {
    authMessage("passwordWrong")
  }

  def tokenMismatch(): Json = {
    authMessage("tokenMismatch")
  }

  def verificationMailMismatch(): Json = {
    authMessage("verificationMailMismatch")
  }

  def tokenNotFound(): Json = {
    authMessage("tokenNotFound")
  }

  def verificationSuccessful(user: User): Json = {
    authMessage(
      "verificationSuccessful",
      messageArguments = List(user.getUserData.nameLogin),
      success = true,
      userOption = Some(user)
    )
  }

  def notLoggedIn(): Json = {
    authMessage("notLoggedIn")
  }

  def formError(errorString: String = ""): Json = {
    authMessage("formError" + errorString, messageArguments = List(errorString))
  }

  def editSuccessful(user: User): Json = {
    authMessage("editSuccessful", success = true, userOption = Some(user))
  }

  def passwordChanged(user: User): Json = {
    authMessage("passwordChanged", success = true, userOption = Some(user))
  }

  def passwordRequestSent(): Json = {
    authMessage("passwordRequestSent", success = true)
  }

  def passwordChangeAccepted(user: User): Json = {
    authMessage("passwordChangeAccepted", success = true, userOption = Some(user))
  }

  def passwordChangeFailed: Json = {
    authMessage("passwordChangeFailed")
  }

  def showPasswordResetView(): Json = {
    authMessage(SHOWPASSWORDRESETVIEW, success = true)
  }

  def noSuchUser: Json = {
    authMessage("noSuchUser")
  }

  def oneParameterNeeded: Json = {
    authMessage("oneParameterNeeded")
  }

  def databaseError: Json = {
    authMessage("databaseError")
  }
}
