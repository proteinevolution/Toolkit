package models.auth

import models.database.users.User
import play.api.libs.json.{JsObject, JsValue, Json}

/**
  * Created by astephens on 26.05.16.
  */
trait JSONTemplate {

  /**
    * Creates a simplified JSON Object from a User Object
    *
    * @param user
    * @return
    */
  def userToJSON(user: User): JsObject = {
    Json.obj("nameLogin" -> user.getUserData.nameLogin)
  }

  /**
    * Creates a JSON Object from an Auth Action Object
    *
    * @param userOption
    * @return
    */
  def authMessage(message: String, success: Boolean = false, userOption: Option[User] = None): JsValue = {
    Json.obj("message"    -> message,
             "successful" -> success,
             "user"       -> userOption.map(user => Json.toJson(user.userData)))
  }

  def LoggedIn(user: User): JsValue = {

    authMessage(s"Welcome, ${user.getUserData.nameLogin}. \n You are now logged in.",
                success = true,
                userOption = Some(user))
  }

  def SignedUp: JsValue = {
    authMessage(s"Your Account has been created.\n Please Check your emails to Verify your Account.", success = true)
  }

  def LoggedOut(): JsValue = {
    authMessage("You have been logged out successfully. See you soon!", success = true)
  }

  def LoginError(): JsValue = {
    authMessage("There was an error while trying to sign you in. Try again!", success = false)
  }

  def AccountNameUsed(): JsValue = {
    authMessage("There already is an Account using this username, please use a different one.")
  }

  def AccountEmailUsed(): JsValue = {
    authMessage("This email is already used, please try a different one.")
  }

  def LoginIncorrect(): JsValue = {
    authMessage("There was an error logging you in. Please check your account name and password.")
  }

  def MustAcceptToS(): JsValue = {
    authMessage("Please accept the terms for our service to register.")
  }

  def MustVerify(): JsValue = {
    authMessage("Please verify your account.\nCheck Your emails for the verification link.")
  }

  def AlreadyLoggedIn(): JsValue = {
    authMessage("You are already logged in.")
  }

  def PasswordMismatch(): JsValue = {
    authMessage("Your passwords did not match.")
  }

  def PasswordWrong(): JsValue = {
    authMessage("The Password was incorrect. Please try again.")
  }

  def TokenMismatch(): JsValue = {
    authMessage("The given token does not match.")
  }

  def TokenNotFound(): JsValue = {
    authMessage("The given token is missing.")
  }

  def VerificationSuccessful(user: User): JsValue = {
    authMessage(s"Your E-Mail Account has been Verified, ${user.getUserData.nameLogin}.",
                success = true,
                userOption = Some(user))
  }

  def NotLoggedIn(): JsValue = {
    authMessage("You are not logged in.")
  }

  def FormError(errorString: String = ""): JsValue = {
    authMessage("There was a Form error:" + errorString)
  }

  def EditSuccessful(user: User): JsValue = {
    authMessage("Changes have been saved.", success = true, userOption = Some(user))
  }

  def PasswordChanged(user: User): JsValue = {
    authMessage("Password has been accepted.\nPlease check your emails in order to verify the password change.",
                success = true,
                userOption = Some(user))
  }

  def PasswordRequestSent: JsValue = {
    authMessage("We have sent You a link for resetting Your password.\nPlease check your emails.", success = true)
  }

  def PasswordResetChanged(user: User): JsValue = {
    authMessage("Password has been accepted.\nPlease sign in with your new Password.",
                success = true,
                userOption = Some(user))
  }

  def NoSuchUser: JsValue = {
    authMessage("Could not find any Users with the matching user name or email address.", success = false)
  }

  def OneParameterNeeded: JsValue = {
    authMessage("Need either a user name or a email address.", success = false)
  }

  def DatabaseError: JsValue = {
    authMessage("The Database could not be reached. Try again later.", success = false)
  }
}
