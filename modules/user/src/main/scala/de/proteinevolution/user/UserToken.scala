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

package de.proteinevolution.user

import java.math.BigInteger
import java.security.SecureRandom
import java.time.ZonedDateTime

import de.proteinevolution.common.models.util.ZonedDateTimeHelper
import reactivemongo.api.bson._

case class UserToken(
    tokenType: Int,
    token: String = UserToken.nextToken(15),
    passwordHash: Option[String] = None,
    eMail: Option[String] = None,
    userID: Option[String] = None,
    changeDate: Option[ZonedDateTime] = Some(ZonedDateTime.now)
)

object UserToken {

  private val random = new SecureRandom()

  def nextToken(nrChars: Int = 24): String = {
    new BigInteger(nrChars * 5, random).toString(32)
  }

  val EMAIL_VERIFICATION_TOKEN = 1
  val PASSWORD_CHANGE_TOKEN    = 2

  val TYPE            = "type"
  val TOKEN           = "token"
  val NEWPASSWORDHASH = "nPWH"
  val NEWEMAIL        = "newEMail"
  val USERID          = "userID"
  val CHANGEDATE      = "changeDate"

  implicit def reader: BSONDocumentReader[UserToken] =
    BSONDocumentReader[UserToken] { bson =>
      UserToken(
        tokenType = bson.getAsOpt[Int](TYPE).getOrElse(-1),
        token = bson.getAsOpt[String](TOKEN).get,
        passwordHash = bson.getAsOpt[String](NEWPASSWORDHASH),
        eMail = bson.getAsOpt[String](NEWEMAIL),
        userID = bson.getAsOpt[String](USERID),
        changeDate = bson.getAsOpt[BSONDateTime](CHANGEDATE).map(ZonedDateTimeHelper.getZDT)
      )
    }

  implicit def writer: BSONDocumentWriter[UserToken] =
    BSONDocumentWriter[UserToken] { userToken =>
      BSONDocument(
        TYPE            -> userToken.tokenType,
        TOKEN           -> userToken.token,
        NEWPASSWORDHASH -> userToken.passwordHash,
        NEWEMAIL        -> userToken.eMail,
        USERID          -> userToken.userID,
        CHANGEDATE      -> BSONDateTime(userToken.changeDate.fold(-1L)(_.toInstant.toEpochMilli))
      )
    }
}
