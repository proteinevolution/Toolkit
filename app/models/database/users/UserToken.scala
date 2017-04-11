package models.database.users

import modules.common.RandomString
import org.joda.time.DateTime
import reactivemongo.bson._

/**
  * Created by astephens on 22.11.16.
  */
case class UserToken(tokenType    : Int,
                     token        : String           = RandomString.randomAlphaNumString(10),
                     passwordHash : Option[String]   = None,
                     eMail        : Option[String]   = None,
                     userID       : Option[BSONObjectID] = None,
                     changeDate   : Option[DateTime] = Some(DateTime.now()))

object UserToken {
  lazy val TYPE             = "type"
  lazy val TOKEN            = "token"
  lazy val NEWPASSWORDHASH  = "nPWH"
  lazy val NEWEMAIL         = "newEMail"
  lazy val USERID           = "userID"
  lazy val CHANGEDATE       = "changeDate"

  /**
    * Object containing the reader for the job state
    */
  implicit object UserTokenReader extends BSONReader[BSONDocument, UserToken] {
    def read(doc: BSONDocument) = UserToken(
      tokenType    = doc.getAs[Int](TYPE).getOrElse(-1),
      token        = doc.getAs[String](TOKEN).get,
      passwordHash = doc.getAs[String](NEWPASSWORDHASH),
      eMail        = doc.getAs[String](NEWEMAIL),
      userID       = doc.getAs[BSONObjectID](USERID),
      changeDate   = doc.getAs[BSONDateTime](CHANGEDATE).map(dt => new DateTime(dt.value)))
  }

  /**
    * Object containing the writer for the job state
    */
  implicit object UserTokenWriter extends BSONWriter[UserToken, BSONDocument] {
    def write(userToken : UserToken) = BSONDocument(
      TYPE            -> userToken.tokenType,
      TOKEN           -> userToken.token,
      NEWPASSWORDHASH -> userToken.passwordHash,
      NEWEMAIL        -> userToken.eMail,
      USERID          -> userToken.userID,
      CHANGEDATE      -> BSONDateTime(userToken.changeDate.fold(-1L)(_.getMillis)))
  }
}