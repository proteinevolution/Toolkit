package models.auth

import javax.inject.{Singleton, Inject}

import models.database.User
import org.mindrot.jbcrypt.BCrypt

/**
  * Created by astephens on 21.04.16.
  */

/**
  * User Manager manages authentication requests
  *
  * @see [[http://www.mindrot.org/files/jBCrypt/jBCrypt-0.2-doc/BCrypt.html#gensalt(int) gensalt]]
  */
@Singleton
class UserManager @Inject ()(userDB : models.database.Users) { // User Database
  val LOG_ROUNDS : Int = 10 // Number of rounds for BCrypt to hash the Password (2^x) // TODO Move to the config?

  /**
    * Checks if a user with the given account name exists and returns the user object
    * @param name_login account name of the User
    * @param name_last  last name of the User
    * @param name_first first name of the User
    * @param email      email of the User
    * @param password   password of the User
    * @return
    */
  def SignUp(name_login : String, name_last : String, name_first : String, email : String, password : String) : AuthAction = {
    userDB.get(name_login) match {
      case Some(user) =>
        AccountNameUsed()
      case None =>
        val newUser = userDB.update(new User(None, name_login,     // Account Name
                                                   name_last,      // Last Name
                                                   name_first,     // First Name
                                                   hash(password), // Immediately hash the password!
                                                   email))         // E-Mail

        LoggedIn(newUser.get)
    }
  }

  /**
    * Checks if the user exists and if the password matches
    * @param name_login account name of the User
    * @param password   password of the User
    * @return
    */
  def SignIn(name_login : String, password : String) : AuthAction = {
    userDB.get(name_login) match {
      case Some(user) =>
        if(checkPassword(user, password)) {
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
  * @param message the message which should be displayed to the User
  */
abstract class AuthAction (val success : Boolean, val message : String)

/**
  * User Login successful
  * @param user User DAO
  */
case class LoggedIn(user : User) extends AuthAction(true,
  "Welcome, " + user.name_last + ". \n You are now logged in.")

/**
  * User Logout succesful
  */
case class LoggedOut()           extends AuthAction(true,
  "You have been logged out successfully. See you soon!")

/**
  * Username has been given away already.
  */
case class AccountNameUsed()     extends AuthAction(false,
  "There already is a Account using this username, please use a different one.")

/**
  * Login was not successful due to an Error
  */
case class LoginIncorrect()      extends AuthAction(false,
  "There was an error logging you in. Please check your account name and password.")

/**
  * User has not accepted the Terms of Service
  */
case class MustAcceptToS()       extends AuthAction(false,
  "Please accept the terms for our service before registering.")

/**
  * Passwords did not match when registering
  */
case class PasswordMismatch()      extends AuthAction(false,
  "Your passwords did not match.")