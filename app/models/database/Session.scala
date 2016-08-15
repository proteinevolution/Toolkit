package models.database

import org.joda.time.DateTime
import reactivemongo.bson._

case class Session(sessionID   : BSONObjectID,          // Session ID of the User
                   userID      : BSONObjectID,          // User ID will be stored
                   dateCreated : Option[DateTime],      // Creation time of the Session
                   dateUpdated : Option[DateTime])      // Last Visit

object Session {
  // Constants for the JSON object identifiers
  val ID            = "id"            // name for the ID in scala
  val IDDB          = "_id"           //              ID in MongoDB
  val SID           = "sid"           //              ID entry in the session cookie
  val USERID        = "userID"        //              ID of the session ID owner
  val STATISTICS    = "statisticsID"  //              ID of the statistics
  val DATECREATED   = "dateCreated"   //              created on field
  val DATEUPDATED   = "dateUpdated"   //              changed on field

  val sessionUserMap = new scala.collection.mutable.HashMap[String, User]

  /**
    * Object containing the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[Session] {
    def read(bson : BSONDocument): Session = {
      Session(
        sessionID   = bson.getAs[BSONObjectID](IDDB).get,
        userID      = bson.getAs[BSONObjectID](USERID).get,
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value)))
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[Session] {
    def write(session: Session) : BSONDocument = BSONDocument(
      IDDB        -> session.sessionID,
      USERID      -> session.userID,
      DATECREATED -> BSONDateTime(session.dateCreated.fold(-1L)(_.getMillis)),
      DATEUPDATED -> BSONDateTime(session.dateUpdated.fold(-1L)(_.getMillis)))
  }
}