package models.auth

import models.database.users.{UserData, User}
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import scala.util.matching.Regex

/**
  * Created by astephens on 03.04.16.
  */

object FormDefinitions {
  // Number of rounds for BCrypt to hash the Password (2^x) TODO Move to the config?
  val LOG_ROUNDS : Int = 10
  // Regex to ensure that any sent strings are not messing up the JSON / BSON
  val textRegex : Regex = """[^\\"\\(\\)\\[\\]]*""".r

  /**
    * Form mapping for the Sign up form
    */
  def SignUp(user: User) = Form(
    mapping(
      UserData.NAMELOGIN -> (text(6,40) verifying pattern(textRegex, error = "error.NameLogin")),
      UserData.PASSWORD  -> (text(8,128) verifying pattern(textRegex, error = "error.Password")),
      UserData.EMAIL     -> email,
      User.ACCEPTEDTOS   -> boolean,
      User.DATELASTLOGIN -> optional(longNumber),
      User.DATECREATED   -> optional(longNumber),
      User.DATEUPDATED   -> optional(longNumber)) {
      (nameLogin, password, eMail, acceptToS, dateLastLogin, dateCreated, dateUpdated) =>
        User(
          userID        = user.userID,
          sessionID     = user.sessionID,
          sessionData   = user.sessionData,
          connected     = user.connected,
          accountType   = if (acceptToS) 1 else 0,
          userData      = Some(UserData(nameLogin = nameLogin,
                                        password  = BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS)),
                                        eMail     = List(eMail))),
          jobs          = user.jobs,
          dateLastLogin = Some(new DateTime()),
          dateCreated   = Some(new DateTime()),
          dateUpdated   = Some(new DateTime())
        )
    } { user =>
      Some((
        user.getUserData.nameLogin,
        "",
        user.getUserData.eMail.head,
        true,
        user.dateLastLogin.map(_.getMillis),
        user.dateCreated.map(_.getMillis),
        user.dateUpdated.map(_.getMillis)
        ))
    }
  )

  /**
    * Form mapping for the Sign in form
    */
  val SignIn = Form(
    mapping(
      UserData.NAMELOGIN -> text(6,40),
      UserData.PASSWORD  -> text(8,128)) {
      (nameLogin, password) =>
        User.Login(
          nameLogin,
          password
        )
    } { user =>
      Some((
        user.nameLogin,
        "******"
        ))
    }
  )

  /**
    * Edit form for the profile
    */
  def ProfileEdit(user : User) = Form(
    mapping(
      UserData.EMAIL     -> email,
      UserData.NAMEFIRST -> optional(text(1,100) verifying pattern(textRegex, error = "error.NameFirst")),
      UserData.NAMELAST  -> optional(text(1,100) verifying pattern(textRegex, error = "error.NameLast")),
      UserData.INSTITUTE -> optional(text(1,100) verifying pattern(textRegex, error = "error.Institute")),
      UserData.STREET    -> optional(text(1,100) verifying pattern(textRegex, error = "error.Street")),
      UserData.CITY      -> optional(text(1,100) verifying pattern(textRegex, error = "error.City")),
      UserData.COUNTRY   -> optional(text(1,100) verifying pattern(textRegex, error = "error.Country")),
      UserData.GROUPS    -> optional(text(1,100) verifying pattern(textRegex, error = "error.Groups")),
      UserData.ROLES     -> optional(text(1,100) verifying pattern(textRegex, error = "error.Roles")),
      UserData.PASSWORD  -> (text(8,128) verifying pattern(textRegex, error = "error.Password"))) {
      (eMail, nameFirst, nameLast, institute, street, city, country, groups, roles, password) =>
        if (user.checkPassword(password)) {
          Some(user.getUserData.copy(nameFirst = nameFirst,
                                     nameLast  = nameLast,
                                     institute = institute,
                                     street    = street,
                                     city      = city,
                                     country   = country,
                                     groups    = groups,
                                     roles     = roles))
        } else {
          None
        }
    } {
      case Some(userData) =>
      Some((userData.eMail.head,
            userData.nameFirst,
            userData.nameLast,
            userData.institute,
            userData.street,
            userData.city,
            userData.country,
            userData.groups,
            userData.roles,
            "******"))
      case None =>
        None
    })

  /**
    * Edit form for the password change in the Profile
    */
  def ProfilePasswordEdit(user : User) = Form(
    mapping(
      UserData.PASSWORDOLD -> (text(8,128) verifying pattern(textRegex, error = "error.OldPassword")),
      UserData.PASSWORDNEW -> (text(8,128) verifying pattern(textRegex, error = "error.NewPassword"))) {
      (passwordOld, passwordNew) =>
        if (user.checkPassword(passwordOld)) {
          Some(BCrypt.hashpw(passwordNew, BCrypt.gensalt(LOG_ROUNDS)))
        } else {
          None
        }
    } {
      case Some(password) =>
        Some(("******","******"))
      case None =>
        None
    }
  )
}