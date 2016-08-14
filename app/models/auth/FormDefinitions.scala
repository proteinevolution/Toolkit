package models.auth

import models.database.{User, UserData}
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import reactivemongo.bson.BSONObjectID

/**
  * Created by astephens on 03.04.16.
  */

object FormDefinitions {
  // Number of rounds for BCrypt to hash the Password (2^x) TODO Move to the config?
  val LOG_ROUNDS : Int = 10
  // Regex to ensure that any sent strings are not messing up the JSON / BSON
  val textRegex = """[^\\"\\(\\)\\[\\]]*""".r

  /**
    * Form mapping for the Sign up form
    */
  val SignUp = Form(
    mapping(
      User.NAMELOGIN     -> (text(6,40) verifying pattern(textRegex, error = "error.NameLogin")),
      UserData.PASSWORD  -> (text(8,128) verifying pattern(textRegex, error = "error.Password")),
      UserData.EMAIL     -> email,
      User.ACCEPTEDTOS   -> boolean,
      User.DATELASTLOGIN -> optional(longNumber),
      User.DATECREATED   -> optional(longNumber),
      User.DATEUPDATED   -> optional(longNumber)) {
      (nameLogin, password, eMail, acceptToS, dateLastLogin, dateCreated, dateUpdated) =>
        User(
          userID        = BSONObjectID.generate,
          nameLogin     = nameLogin,
          accountType   = if (acceptToS) 1 else 0,
          userData      = Some(UserData(password = BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS)),
                                        eMail    = eMail,
                                        None,
                                        None,
                                        None,
                                        None,
                                        None,
                                        None,
                                        None,
                                        None)),
          jobs          = Nil,
          dateLastLogin = Some(new DateTime()),
          dateCreated   = Some(new DateTime()),
          dateUpdated   = Some(new DateTime())
        )
    } { user =>
      Some((
        user.nameLogin,
        "",
        user.userData.get.eMail,
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
      User.NAMELOGIN    -> text(6,40),
      UserData.PASSWORD -> text(8,128)) {
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
  val ProfileEdit = Form(
    mapping(
      UserData.EMAIL     -> email,
      UserData.NAMEFIRST -> optional(text(1,100)),
      UserData.NAMELAST  -> optional(text(1,100)),
      UserData.INSTITUTE -> optional(text(1,100)),
      UserData.STREET    -> optional(text(1,100)),
      UserData.CITY      -> optional(text(1,100)),
      UserData.COUNTRY   -> optional(text(1,100)),
      UserData.GROUPS    -> optional(text(1,100)),
      UserData.ROLES     -> optional(text(1,100)),
      UserData.PASSWORD  -> (text(8,128) verifying pattern(textRegex, error = "error.Password"))) {
      (eMail, nameFirst, nameLast, institute, street, city, country, groups, roles, password) =>
        UserData.EditProfileForm(eMail,
                                 nameFirst,
                                 nameLast,
                                 institute,
                                 street,
                                 city,
                                 country,
                                 groups,
                                 roles,
                                 password)
    } { userData =>
      Some((
        userData.eMail,
        userData.nameFirst,
        userData.nameLast,
        userData.institute,
        userData.street,
        userData.city,
        userData.country,
        userData.groups,
        userData.roles,
        "******"))
    })

  /**
    * Edit form for the password change in the Profile
    */
  val ProfilePasswordEdit = Form(
    mapping(
      UserData.PASSWORDOLD -> (text(8,128) verifying pattern(textRegex, error = "error.Password")),
      UserData.PASSWORDNEW -> (text(8,128) verifying pattern(textRegex, error = "error.NewPassword"))) {
      (password, passwordNew) =>
        UserData.UpdatePasswordForm(password, passwordNew)
    } { password =>
      Some(("******","******"))
    }
  )
}




/**
  * Login form for the dashboard
 */

trait backendLogin {

  val loginForm = Form(
    tuple(
      "user_login"    -> nonEmptyText,
      "password"      -> nonEmptyText
    )
  )
}

/**
  * Form mapping for the forgot login/password form
  */
object Forgot {
  val inputForm = Form(
    tuple(
      "name_login"    -> nonEmptyText,
      "email"         -> email
    )
  )
}
