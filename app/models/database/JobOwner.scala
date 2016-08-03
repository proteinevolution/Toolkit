package models.database

import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader, BSONObjectID}

/**
  * Created by astephens on 03.08.16.
  */
case class JobOwner(sessionIDOpt : Option[BSONObjectID], userIDOpt : Option[BSONObjectID])
object JobOwner {
  val USERID        = "userID"        //              ID of the User
  val SESSIONID     = "sessionID"     //              ID of the Session

  /**
    * Object containing the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[JobOwner] {
    def read(bson : BSONDocument): JobOwner = {
      JobOwner(sessionIDOpt = bson.getAs[BSONObjectID](SESSIONID),
               userIDOpt    = bson.getAs[BSONObjectID](USERID))
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[JobOwner] {
    def write(job: JobOwner) : BSONDocument = BSONDocument(
      SESSIONID  -> job.sessionIDOpt,
      USERID     -> job.userIDOpt)
  }
}