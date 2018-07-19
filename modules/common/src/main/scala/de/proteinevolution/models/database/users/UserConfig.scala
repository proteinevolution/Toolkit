package de.proteinevolution.models.database.users

import reactivemongo.bson._

case class UserConfig(
    defaultPublic: Boolean = false,
    defaultComments: Boolean = false,
    hasMODELLERKey: Boolean = false
)

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
