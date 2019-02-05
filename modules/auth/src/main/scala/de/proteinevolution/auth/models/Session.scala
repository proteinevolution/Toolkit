package de.proteinevolution.auth.models

import reactivemongo.bson.BSONObjectID

object Session {

  case class ChangeSessionID(sessionID: BSONObjectID)

}
