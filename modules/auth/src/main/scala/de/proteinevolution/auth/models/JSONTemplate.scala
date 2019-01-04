package de.proteinevolution.auth.models

import de.proteinevolution.models.database.users.User
import io.circe.Json
import io.circe.syntax._

trait JSONTemplate {

  def userToJSON(user: User): Json = {
    Json.obj("nameLogin" -> Json.fromString(user.getUserData.nameLogin))
  }

  def authMessage(message: String, success: Boolean = false, userOption: Option[User] = None): Json = {
    Json.obj("message"    -> Json.fromString(message),
             "successful" -> Json.fromBoolean(success),
             "user"       -> userOption.map(user => user.userData.asJson).getOrElse(Json.Null))
  }

  def loggedIn(user: User): Json = {

    authMessage(s"Welcome, ${user.getUserData.nameLogin}. \n You are now logged in.",
                success = true,
                userOption = Some(user))
  }

  def signedUp: Json = {
    authMessage(s"Your Account has been created.\n Please Check your emails to Verify your Account.", success = true)
  }

  def loggedOut(): Json = {
    authMessage("You have been logged out successfully. See you soon!", success = true)
  }

  def loginError(): Json = {
    authMessage("There was an error while trying to sign you in. Try again!")
  }

  def accountNameUsed(): Json = {
    authMessage("There already is an Account using this username, please use a different one.")
  }

  def accountEmailUsed(): Json = {
    authMessage("This email is already used, please try a different one.")
  }

  def loginIncorrect(): Json = {
    authMessage("There was an error logging you in. Please check your account name and password.")
  }

  def mustAcceptToS(): Json = {
    authMessage("Please accept the terms for our service to register.")
  }

  def mustVerify(): Json = {
    authMessage("Please verify your account.\nCheck Your emails for the verification link.")
  }

  def alreadyLoggedIn(): Json = {
    authMessage("You are already logged in.")
  }

  def passwordMismatch(): Json = {
    authMessage("Your passwords did not match.")
  }

  def passwordWrong(): Json = {
    authMessage("The Password was incorrect. Please try again.")
  }

  def tokenMismatch(): Json = {
    authMessage("The given token does not match.")
  }

  def tokenNotFound(): Json = {
    authMessage("The given token is missing.")
  }

  def verificationSuccessful(user: User): Json = {
    authMessage(s"Your E-Mail Account has been Verified, ${user.getUserData.nameLogin}.",
                success = true,
                userOption = Some(user))
  }

  def notLoggedIn(): Json = {
    authMessage("You are not logged in.")
  }

  def formError(errorString: String = ""): Json = {
    authMessage("There was a Form error:" + errorString)
  }

  def editSuccessful(user: User): Json = {
    authMessage("Changes have been saved.", success = true, userOption = Some(user))
  }

  def passwordChanged(user: User): Json = {
    authMessage("Password has been accepted.\nPlease check your emails in order to verify the password change.",
                success = true,
                userOption = Some(user))
  }

  def passwordRequestSent: Json = {
    authMessage("We have sent You a link for resetting Your password.\nPlease check your emails.", success = true)
  }

  def passwordResetChanged(user: User): Json = {
    authMessage("Password has been accepted.\nPlease sign in with your new Password.",
                success = true,
                userOption = Some(user))
  }

  def noSuchUser: Json = {
    authMessage("Could not find any Users with the matching user name or email address.")
  }

  def oneParameterNeeded: Json = {
    authMessage("Need either a user name or a email address.")
  }

  def databaseError: Json = {
    authMessage("The Database could not be reached. Try again later.")
  }

}
