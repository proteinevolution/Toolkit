package models.database

import modules.common.RandomString
import org.joda.time.DateTime
import reactivemongo.bson._

/**
  * Created by astephens on 22.11.16.
  */
object UserToken {
  sealed trait UserToken {
    val tokenType  : BSONInteger
    val token      : String
    val changeDate : Option[DateTime]
  }

  case class VerifyAccount(token      : String = RandomString.randomAlphaNumString(15),
                           changeDate : Option[DateTime] = Some(DateTime.now)) extends UserToken {
    val tokenType = BSONInteger(0)
  }
  case class ResetPassword(token      : String = RandomString.randomAlphaNumString(15),
                           changeDate : Option[DateTime] = Some(DateTime.now)) extends UserToken {
    val tokenType = BSONInteger(1)
  }
  case class VerifyPassword(token           : String = RandomString.randomAlphaNumString(15),
                        val newPasswordHash : String,
                            changeDate      : Option[DateTime] = Some(DateTime.now)) extends UserToken {
    val tokenType = BSONInteger(2)
  }

  lazy val TYPE       = "type"
  lazy val TOKEN      = "token"
  lazy val NEWPWHASH  = "nPWH"
  lazy val CHANGEDATE = "changeDate"

  /**
    * Object containing the reader for the job state
    */
  implicit object UserTokenReader extends BSONReader[BSONDocument, UserToken] {
    def read(doc: BSONDocument) = {
      doc.getAs[BSONInteger](TYPE).get match {
        case BSONInteger(0) => VerifyAccount(
          token      = doc.getAs[String](TOKEN).get,
          changeDate = doc.getAs[BSONDateTime](CHANGEDATE).map(dt => new DateTime(dt.value))
        )
        case BSONInteger(1) => ResetPassword(
          token      = doc.getAs[String](TOKEN).get,
          changeDate = doc.getAs[BSONDateTime](CHANGEDATE).map(dt => new DateTime(dt.value))
        )
        case BSONInteger(2) => VerifyPassword(
          token      = doc.getAs[String](TOKEN).get,
          newPasswordHash = doc.getAs[String](NEWPWHASH).get,
          changeDate = doc.getAs[BSONDateTime](CHANGEDATE).map(dt => new DateTime(dt.value))
        )
      }
    }
  }

  /**
    * Object containing the writer for the job state
    */
  implicit object UserTokenWriter extends BSONWriter[UserToken, BSONDocument] {
    def write(userToken : UserToken)  = {
      userToken match {
        case VerifyAccount(token, changeDate) =>
          BSONDocument(TYPE       -> BSONInteger(0),
                       TOKEN      -> token,
                       CHANGEDATE -> BSONDateTime(changeDate.fold(-1L)(_.getMillis)))
        case ResetPassword(token, changeDate) =>
          BSONDocument(TYPE       -> BSONInteger(1),
                       TOKEN      -> token,
                       CHANGEDATE -> BSONDateTime(changeDate.fold(-1L)(_.getMillis)))
        case VerifyPassword(token, newPasswordHash, changeDate) =>
          BSONDocument(TYPE       -> BSONInteger(1),
                       TOKEN      -> token,
                       NEWPWHASH  -> newPasswordHash,
                       CHANGEDATE -> BSONDateTime(changeDate.fold(-1L)(_.getMillis)))
      }
    }
  }
}