/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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

import de.proteinevolution.user.{ User, UserData, UserToken }
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
      UserData.NAME_LOGIN  -> text(6, 40).verifying(pattern(textRegex, error = "error.NameLogin")),
      UserData.PASSWORD    -> text(8, 128).verifying(pattern(textRegex, error = "error.Password")),
      UserData.EMAIL       -> email,
      User.ACCEPTED_TOS    -> boolean,
      User.DATE_LAST_LOGIN -> optional(longNumber),
      User.DATE_CREATED    -> optional(longNumber),
      User.DATE_UPDATED    -> optional(longNumber)
    ) { (nameLogin, password, eMail, acceptToS, _, _, _) =>
      User(
        userID = user.userID,
        sessionID = user.sessionID,
        sessionData = user.sessionData,
        connected = user.connected,
        accountType = if (acceptToS) 0 else -1,
        userData = Some(
          UserData(nameLogin = nameLogin, password = BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS)), eMail = eMail)
        ),
        jobs = user.jobs
      )
    } { _ =>
      None
    }
  )

  /**
   * Form mapping for the Sign in form
   */
  lazy val signIn = Form(
    mapping(UserData.NAME_LOGIN -> text(6, 40), UserData.PASSWORD -> text(8, 128)) { (nameLogin, password) =>
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
        UserData.EMAIL      -> optional(email),
        UserData.NAME_FIRST -> optional(text(1, 25).verifying(pattern(textRegex, error = "error.NameFirst"))),
        UserData.NAME_LAST  -> optional(text(1, 25).verifying(pattern(textRegex, error = "error.NameLast"))),
        UserData.COUNTRY    -> optional(text(3, 3).verifying(pattern(textRegex, error = "error.Country"))),
        UserData.PASSWORD   -> text(8, 128).verifying(pattern(textRegex, error = "error.Password"))
      ) { (eMail, nameFirst, nameLast, country, password) =>
        if (user.checkPassword(password)) {
          Some(
            user.userData.get.copy(
              eMail = eMail.getOrElse(user.userData.get.eMail),
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
      UserData.PASSWORD_OLD -> text(8, 128).verifying(pattern(textRegex, error = "error.OldPassword")),
      UserData.PASSWORD_NEW -> text(8, 128).verifying(pattern(textRegex, error = "error.NewPassword"))
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

  def forgottenPasswordRequest = Form(
    mapping(UserData.EMAIL_OR_USERNAME -> nonEmptyText.verifying(pattern(textRegex))) {
      Some(_)
    } { _ =>
      None
    }
  )

  def forgottenPasswordChange = Form(
    mapping(
      UserData.PASSWORD_NEW -> text(8, 128).verifying(pattern(textRegex, error = "error.NewPassword")),
      UserData.NAME_LOGIN   -> text(6, 40),
      UserToken.TOKEN       -> text(15, 15)
    ) { (passwordNew, nameLogin, token) =>
      (BCrypt.hashpw(passwordNew, BCrypt.gensalt(LOG_ROUNDS)), nameLogin, token)
    } { _ =>
      None
    }
  )
}
