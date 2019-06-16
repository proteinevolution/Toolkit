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

import java.time.ZonedDateTime

import de.proteinevolution.user.{ User, UserData }
import org.mindrot.jbcrypt.BCrypt
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import scala.util.matching.Regex

object FormDefinitions {
  // Number of rounds for BCrypt to hash the Password (2^x) TODO Move to the config?
  val LOG_ROUNDS: Int = 10
  // Regex to ensure that any sent strings are not messing up the JSON / BSON
  val textRegex: Regex = """[^\\"\\(\\)\\[\\]]*""".r

  /**
   * Form mapping for the Sign up form
   */
  def signUp(user: User) = Form(
    mapping(
      UserData.NAMELOGIN -> text(6, 40).verifying(pattern(textRegex, error = "error.NameLogin")) ,
      UserData.PASSWORD  -> text(8, 128).verifying(pattern(textRegex, error = "error.Password")) ,
      UserData.EMAIL     -> email,
      User.ACCEPTEDTOS   -> boolean,
      User.DATELASTLOGIN -> optional(longNumber),
      User.DATECREATED   -> optional(longNumber),
      User.DATEUPDATED   -> optional(longNumber)
    ) { (nameLogin, password, eMail, acceptToS, _, _, _) =>
      import de.proteinevolution.user.UserData
      User(
        userID = user.userID,
        sessionID = user.sessionID,
        sessionData = user.sessionData,
        connected = user.connected,
        accountType = if (acceptToS) 0 else -1,
        userData = Some(
          UserData(nameLogin = nameLogin, password = BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS)), eMail = eMail)
        ),
        jobs = user.jobs,
        dateLastLogin = Some(ZonedDateTime.now),
        dateCreated = Some(ZonedDateTime.now),
        dateUpdated = Some(ZonedDateTime.now)
      )
    } { _ =>
      None
    }
  )

  /**
   * Form mapping for the Sign in form
   */
  lazy val signIn = Form(
    mapping(UserData.NAMELOGIN -> text(6, 40), UserData.PASSWORD -> text(8, 128)) { (nameLogin, password) =>
      User.Login(
        nameLogin,
        password
      )
    } { _ =>
      None
    }
  )

  /**
   * Edit form for the profile
   */
  def profileEdit(user: User) =
    Form(
      mapping(
        UserData.EMAIL     -> optional(email),
        UserData.NAMEFIRST -> optional(text(1, 25).verifying(pattern(textRegex, error = "error.NameFirst"))),
        UserData.NAMELAST  -> optional(text(1, 25).verifying(pattern(textRegex, error = "error.NameLast"))),
        UserData.COUNTRY   -> optional(text(3, 3).verifying(pattern(textRegex, error = "error.Country"))),
        UserData.PASSWORD  -> text(8, 128).verifying(pattern(textRegex, error = "error.Password"))
      ) { (eMail, nameFirst, nameLast, country, password) =>
        if (user.checkPassword(password)) {
          Some(
            user.getUserData.copy(
              eMail = eMail.getOrElse(user.getUserData.eMail),
              nameFirst = nameFirst,
              nameLast = nameLast,
              country = country
            )
          )
        } else {
          None
        }
      } { _ =>
        None
      }
    )

  /**
   * Edit form for the password change in the Profile
   */
  def profilePasswordEdit(user: User) = Form(
    mapping(
      UserData.PASSWORDOLD -> text(8, 128).verifying(pattern(textRegex, error = "error.OldPassword")),
      UserData.PASSWORDNEW -> text(8, 128).verifying(pattern(textRegex, error = "error.NewPassword"))
    ) { (passwordOld, passwordNew) =>
      if (user.checkPassword(passwordOld)) {
        Some(BCrypt.hashpw(passwordNew, BCrypt.gensalt(LOG_ROUNDS)))
      } else {
        None
      }
    } { _ =>
      None
    }
  )

  def forgottenPasswordEdit = Form(
    mapping(UserData.EMAIL -> email) {
      Some(_)
    } { _ =>
      None
    }
  )

  def forgottenPasswordChange = Form(
    mapping(UserData.PASSWORDNEW -> text(8, 128).verifying(pattern(textRegex, error = "error.NewPassword"))) {
      passwordNew =>
        BCrypt.hashpw(passwordNew, BCrypt.gensalt(LOG_ROUNDS))
    } { _ =>
      None
    }
  )
}
