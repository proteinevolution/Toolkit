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

import io.circe.Encoder
import io.circe.generic.semiauto._
import reactivemongo.api.bson._

case class UserData(
    nameLogin: String,
    password: String,
    eMail: String,
    nameFirst: Option[String] = None,
    nameLast: Option[String] = None,
    country: Option[String] = None
)

object UserData {

  final val NAME_LOGIN        = "nameLogin"
  final val PASSWORD          = "password"
  final val PASSWORD_OLD      = "passwordOld"
  final val EMAIL             = "eMail"
  final val PASSWORD_NEW      = "passwordNew"
  final val NAME_FIRST        = "nameFirst"
  final val NAME_LAST         = "nameLast"
  final val COUNTRY           = "country"
  final val EMAIL_OR_USERNAME = "eMailOrUsername"

  implicit val userDataEncoder: Encoder[UserData] = deriveEncoder[UserData]

  implicit val userDataBSONHandler: BSONDocumentHandler[UserData] = Macros.handler[UserData]

}
