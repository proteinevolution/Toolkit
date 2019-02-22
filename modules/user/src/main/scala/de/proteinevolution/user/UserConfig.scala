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

import reactivemongo.bson._

case class UserConfig(defaultPublic: Boolean = false, defaultComments: Boolean = false, hasMODELLERKey: Boolean = false)

object UserConfig {
  final val DEFAULTPUBLIC   = "defaultPublic"
  final val DEFAULTCOMMENTS = "defaultComments"
  final val HASMODELLERKEY  = "modellerKey"

  implicit object Reader extends BSONDocumentReader[UserConfig] {
    override def read(bson: BSONDocument): UserConfig = UserConfig(
      defaultPublic = bson.getAs[Boolean](DEFAULTPUBLIC).getOrElse(false),
      defaultComments = bson.getAs[Boolean](DEFAULTCOMMENTS).getOrElse(false),
      hasMODELLERKey = bson.getAs[Boolean](HASMODELLERKEY).getOrElse(false)
    )
  }

  implicit object Writer extends BSONDocumentWriter[UserConfig] {
    override def write(userConfig: UserConfig): BSONDocument =
      if (userConfig == UserConfig())
        BSONDocument.empty
      else
        BSONDocument(
          DEFAULTPUBLIC   -> BSONBoolean(userConfig.defaultPublic),
          DEFAULTCOMMENTS -> BSONBoolean(userConfig.defaultPublic),
          HASMODELLERKEY  -> BSONBoolean(userConfig.hasMODELLERKey)
        )
  }
}
