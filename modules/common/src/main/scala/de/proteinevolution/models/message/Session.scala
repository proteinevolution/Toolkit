package de.proteinevolution.models.message

import reactivemongo.bson.BSONObjectID

object Session {

  case class ChangeSessionID(sessionID: BSONObjectID)

}
