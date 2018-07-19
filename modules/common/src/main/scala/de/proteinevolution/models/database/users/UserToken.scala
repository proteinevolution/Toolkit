package de.proteinevolution.models.database.users

import java.time.ZonedDateTime
import java.security.SecureRandom
import java.math.BigInteger
import reactivemongo.bson._
import de.proteinevolution.models.util.ZonedDateTimeHelper

case class UserToken(
    tokenType: Int,
    token: String = UserToken.nextToken(15),
    passwordHash: Option[String] = None,
    eMail: Option[String] = None,
    userID: Option[BSONObjectID] = None,
    changeDate: Option[ZonedDateTime] = Some(ZonedDateTime.now)
)

object UserToken {

  private val random = new SecureRandom()

  def nextToken(nrChars: Int = 24): String = {
    new BigInteger(nrChars * 5, random).toString(32)
  }

  val TYPE            = "type"
  val TOKEN           = "token"
  val NEWPASSWORDHASH = "nPWH"
  val NEWEMAIL        = "newEMail"
  val USERID          = "userID"
  val CHANGEDATE      = "changeDate"

  /**
   * Object containing the reader for the job state
   */
  implicit object UserTokenReader extends BSONReader[BSONDocument, UserToken] {
    def read(doc: BSONDocument) =
      UserToken(
        tokenType = doc.getAs[Int](TYPE).getOrElse(-1),
        token = doc.getAs[String](TOKEN).get,
        passwordHash = doc.getAs[String](NEWPASSWORDHASH),
        eMail = doc.getAs[String](NEWEMAIL),
        userID = doc.getAs[BSONObjectID](USERID),
        changeDate = doc
          .getAs[BSONDateTime](CHANGEDATE)
          .map(dt => ZonedDateTimeHelper.getZDT(dt))
      )
  }

  /**
   * Object containing the writer for the job state
   */
  implicit object UserTokenWriter extends BSONWriter[UserToken, BSONDocument] {
    def write(userToken: UserToken) =
      BSONDocument(
        TYPE            -> userToken.tokenType,
        TOKEN           -> userToken.token,
        NEWPASSWORDHASH -> userToken.passwordHash,
        NEWEMAIL        -> userToken.eMail,
        USERID          -> userToken.userID,
        CHANGEDATE -> BSONDateTime(
          userToken.changeDate.fold(-1L)(_.toInstant.toEpochMilli)
        )
      )
  }
}
