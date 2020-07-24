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

import reactivemongo.api.bson._

import scala.util.{ Success, Try }

case class UserConfig(defaultPublic: Boolean = false, defaultComments: Boolean = false, hasMODELLERKey: Boolean = false)

object UserConfig {
  final val DEFAULTPUBLIC   = "defaultPublic"
  final val DEFAULTCOMMENTS = "defaultComments"
  final val HASMODELLERKEY  = "modellerKey"

  implicit object Reader extends BSONDocumentReader[UserConfig] {
    override def readDocument(bson: BSONDocument): Try[UserConfig] =
      Success(
        UserConfig(
          defaultPublic = bson.getAsOpt[Boolean](DEFAULTPUBLIC).getOrElse(false),
          defaultComments = bson.getAsOpt[Boolean](DEFAULTCOMMENTS).getOrElse(false),
          hasMODELLERKey = bson.getAsOpt[Boolean](HASMODELLERKEY).getOrElse(false)
        )
      )
  }

  implicit object Writer extends BSONDocumentWriter[UserConfig] {
    override def writeTry(userConfig: UserConfig): Try[BSONDocument] =
      Success(
        if (userConfig == UserConfig())
          BSONDocument.empty
        else
          BSONDocument(
            DEFAULTPUBLIC   -> BSONBoolean(userConfig.defaultPublic),
            DEFAULTCOMMENTS -> BSONBoolean(userConfig.defaultPublic),
            HASMODELLERKEY  -> BSONBoolean(userConfig.hasMODELLERKey)
          )
      )
  }
}
