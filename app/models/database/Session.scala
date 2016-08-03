package models.database

import org.joda.time.DateTime
import reactivemongo.bson._

case class Session(sessionID   : BSONObjectID,          // Session ID of the User
                   userID      : Option[BSONObjectID],  // If the User is logged in, the ID will be stored
                   jobIDs      : List[BSONObjectID],    // Otherwise the Job IDs are stored here
                   dateCreated : Option[DateTime],      // Creation time of the Session
                   dateUpdated : Option[DateTime])      // Last Visit

object Session {
  // Constants for the JSON object identifiers
  val ID            = "id"            // name for the ID in scala
  val IDDB          = "_id"           //              ID in MongoDB
  val USERID        = "userID"        //              ID of the session ID owner, when they are logged in
  val JOBIDS        = "jobIDs"        //              IDs of jobs a unregistered user has
  val DATECREATED   = "dateCreated"   //              created on field
  val DATEUPDATED   = "dateUpdated"   //              changed on field

  /**
    * Object containing the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[Session] {
    def read(bson : BSONDocument): Session = {
      Session(
        sessionID   = bson.getAs[BSONObjectID](IDDB).get,
        userID      = bson.getAs[BSONObjectID](USERID),
        jobIDs      = bson.getAs[List[BSONObjectID]](JOBIDS).get,
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value)))
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[Session] {
    def write(session: Session) : BSONDocument = BSONDocument(
      ID          -> session.sessionID,
      USERID      -> session.userID,
      JOBIDS      -> session.jobIDs,
      DATECREATED -> BSONDateTime(session.dateCreated.fold(-1L)(_.getMillis)),
      DATEUPDATED -> BSONDateTime(session.dateUpdated.fold(-1L)(_.getMillis)))
  }
}