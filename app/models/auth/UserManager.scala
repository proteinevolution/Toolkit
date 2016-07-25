package models.auth

import javax.inject.{Singleton, Inject}

import models.database.{UserVerification, User}
import models.mailing.NewUserWelcomeMail
import models.misc.RandomString
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
final class UserManager @Inject ()(userDB             : models.database.Users,             // User Database
                             userVerificationDB : models.database.UserVerifications, // Verification Database
                             mailing            : controllers.Mailing) {             // Mailing Controller
  val LOG_ROUNDS : Int = 10 // Number of rounds for BCrypt to hash the Password (2^x) // TODO Move to the config?

  /**
    * Checks if a user with the given account name exists and returns the user object
    *
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
        val user = userDB.update(new User(None, name_login,     // Account Name
                                                name_last,      // Last Name
                                                name_first,     // First Name
                                                hash(password), // Immediately hash the password!
                                                email/*,        // E-Mail
                                                //None,         // Address*/))
        val token = RandomString.randomAlphaNumString(7)
        userVerificationDB.add(new UserVerification(user.get.user_id.get,       // User ID
                                                    token,                      // Token
                                                    'e'))                       // Token type "Email Verification"
        mailing.sendEmail(user.get, new NewUserWelcomeMail(token)) // Send a E-Mail to the User to verify the address
        LoggedIn(user.get)
    }
  }

  /**
    * Checks if the user exists and if the password matches
    *
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
    * Checks if the backend user is authorized
    */
  def backendLogin(name_login : String, password: String) : AuthAction = {

    val testUser = new User(Some(12345), "test", "Alva", "Vikram", "test", "vikram.alva@tuebingen.mpg.de")


    name_login match {

      case testUser.name_login =>
        if (password.equals("test")) {
          LoggedIn(testUser)
        }
        else {

          LoginIncorrect()
        }

      case _ =>
        LoginIncorrect()
    }
  }

  /**
    * Verifies a User's token
    *
    * @param name_login
    * @param token
    * @return
    */
  def VerifyEmail(name_login : String, token : String) : AuthAction = {
    val user_o = userDB.get(name_login)   // Get the user from the database
    user_o match {
      case Some(user) =>                  // User exists.
        val verification_o = userVerificationDB.get(user.user_id.get)
        verification_o match {
          case Some(verification) =>      // Verification token exists.
            if (verification.token.matches(token)) {
              userVerificationDB.remove(user.user_id.get)
              VerificationSuccessful(user)
            } else {
              TokenMismatch()
            }
          case None =>
            TokenMismatch()
        }
      case None =>
        LoginIncorrect()
    }
  }

  /**
    * More general Functions
    */

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
  *
  * @param success whether the action was successful or not
  * @param message the message which should be displayed to the User
  */
abstract class AuthAction (val success : Boolean = false, val message : String, val user_o : Option[User] = None)

/**
  * User Login successful
  *
  * @param user User DAO
  */
case class LoggedIn(user : User) extends AuthAction(true,
  "Welcome, " + user.name_last + ". \n You are now logged in.",
  Some(user))

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

/**
  * Verification was successful
  */
case class VerificationSuccessful(user : User) extends AuthAction(true,
  "Your E-Mail Account has been Verified, "+user.name_login+".",
  Some(user))

/**
  * Passwords did not match when registering
  */
case class TokenMismatch()      extends AuthAction(false,
  "Your Token did not match.")