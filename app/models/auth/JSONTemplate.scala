package models.auth

import models.database.User
import play.api.libs.json.{JsValue, Json}

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
  def userToJSON (user : User) = {
    Json.obj("nameLogin" -> user.getUserData.nameLogin)
  }

  /**
    * Creates a JSON Object from an Auth Action Object
    *
    * @param userOption
    * @return
    */
  def authMessage(message : String, success : Boolean, userOption : Option[User]) = {
    Json.toJson(userOption match {
      case Some(user) =>
        Json.obj("message"    -> message,
                 "successful" -> success,
                 "user"       -> userToJSON(user))
      case None =>
        Json.obj("message"    -> message,
                 "successful" -> success)
    })
  }

  def LoggedIn(user : User) = {
    authMessage("Welcome, " + user.getUserData.nameLogin + ". \n You are now logged in.",
                true,
                Some(user))
  }

  def LoggedOut() = {
    authMessage("You have been logged out successfully. See you soon!",
                true,
                None)
  }


  def LoginError() = {
    authMessage("There was an error while trying to sign you in. Try again!",
                false,
                None)
  }

  def AccountNameUsed() = {
    authMessage("There already is an Account using this username, please use a different one.",
                false,
                None)
  }

  def LoginIncorrect() = {
    authMessage("There was an error logging you in. Please check your account name and password.",
                false,
                None)
  }

  def MustAcceptToS() = {
    authMessage("Please accept the terms for our service to register.",
      false,
      None)
  }

  def AlreadyLoggedIn() = {
    authMessage("You are already logged in.",
      false,
      None)
  }

  def PasswordMismatch() = {
    authMessage("Your passwords did not match.",
                false,
                None)
  }

  def PasswordWrong() = {
    authMessage("The Password was incorrect. Please try again.",
                false,
                None)
  }

  def TokenMismatch() = {
    authMessage("The given token does not match.",
                false,
                None)
  }

  def VerificationSuccessful(user : User) = {
    authMessage("Your E-Mail Account has been Verified, "+user.getUserData.nameLogin+".",
                true,
                Some(user))
  }

  def NotLoggedIn() = {
    authMessage("You are not logged in.",
                false,
                None)
  }

  def FormError() = {
    authMessage("There was a Form error.",
                false,
                None)
  }

  def EditSuccessful(user : User) = {
    authMessage("Changes have been saved.",
                true,
                Some(user))
  }
}
