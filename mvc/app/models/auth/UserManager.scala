package models.auth

import javax.inject.{Singleton, Inject}

import models.database.User
import org.mindrot.jbcrypt.BCrypt

/**
  * Created by astephens on 21.04.16.
  */

/**
  * User Manager manages authentication requests
  * @see [[http://www.mindrot.org/files/jBCrypt/jBCrypt-0.2-doc/BCrypt.html#gensalt(int) gensalt]]
  */
@Singleton
class UserManager @Inject ()(userDB : models.database.Users) { // User Database
  val LOG_ROUNDS : Int = 10 // Number of rounds for BCrypt to hash the Password (2^x) // TODO Move to the config?

  /**
    * Checks if a user with the given email exists and
    *
    * @param form
    * @return
    */
  def SignUp(form : Map[String,String]) : AuthAction = {
    userDB.get(form.get("email").get) match {
      case Some(user) =>
        EmailUsed()
      case None =>
        val newUser = userDB.update(new User(None, "basic",
                                                   form.get("name_last").get,       // Last Name
                                                   form.get("name_first").get,      // First Name
                                                   hash(form.get("password").get),  // Immediately hash the password!
                                                   form.get("email").get))          // E-Mail

        LoggedIn(newUser.get)
    }
  }

  /**
    * Checks if the User Exists and if the Password matches
    *
    * @param form input form
    * @return
    */
  def SignIn(form : Map[String, String]) : AuthAction = {
    userDB.get(form.get("email").get) match {
      case Some(user) =>
        if(checkPassword(user, form.get("password").get)) {
          LoggedIn(user)
        } else {
          LoginIncorrect()
        }
      case None =>
        LoginIncorrect()
    }
  }

  /**
    * Hashes a password.
    *
    * This implementation does not return the salt separately because it is embedded in the hashed password.
    * Other implementations might need to return it so it gets saved in the backing store.
    *
    * @param plainPassword The password to hash.
    * @return A PasswordInfo containing the hashed password.
    */
  def hash(plainPassword: String) = BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS))

  /**
    * Checks if a password matches the hashed version.
    *
    * @param plainPassword The password supplied by the user.
    * @return True if the password matches, false otherwise.
    */
  def checkPassword(user : User, plainPassword: String) = {
    BCrypt.checkpw(plainPassword, user.password)
  }
}

/**
  * Abstract object returning whether the Action was successful or not
  * @param success whether the action was successful or not
  */
abstract class AuthAction (val success : Boolean)

/**
  * User Login successful
  * @param user User DAO
  */
case class LoggedIn(user : User) extends AuthAction(true)

/**
  * User Logout succesful
  */
case class LoggedOut()           extends AuthAction(true)

/**
  * EMail has been used to identify the user already
  */
case class EmailUsed()           extends AuthAction(false)

/**
  * Login was not successful due to an Error
  */
case class LoginIncorrect()      extends AuthAction(false)