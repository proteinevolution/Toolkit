package models.database

import javax.inject.{Inject, Singleton}

import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Class for database storage of user verification codes
 *
  * @param tag
  */

class UserVerificationsTableDef(tag: Tag) extends Table[UserVerification](tag, "userverification") {
  def user_id    = column[Long]("user_id")
  def token      = column[String]("token")
  def token_type = column[Char]("token_type")
  def token_exp  = column[Long]("token_exp")

  override def * = (user_id, token, token_type) <> (UserVerification.tupled, UserVerification.unapply)
}

@Singleton
class UserVerifications @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val userVerification = TableQuery[UserVerificationsTableDef]

  /**
    * Adds a User Verification token to the database
 *
    * @return
    */
  def add(verification: UserVerification) : UserVerification = {
    Await.result(dbConfig.db.run(userVerification += verification), Duration.Inf)
    verification
  }

  /**
    * Find users in the Database with the matching filter
 *
    * @param user_id ID for primary key of Jobs
    * @return
    */
  def get (user_id : Long) : Option[UserVerification] = {
    Await.result(dbConfig.db.run(userVerification.filter(_.user_id === user_id).result), Duration.Inf).headOption
  }

  /**
    * Deletes the security Token
 *
    * @param user_id
    * @return
    */
  def remove (user_id : Long) = {
    Await.result(dbConfig.db.run(userVerification.filter(_.user_id === user_id).delete), Duration.Inf)
  }
}


// Verification Data Object used for database storage and interaction
case class UserVerification(val user_id : Long, val token : String, val token_type : Char) {
  val tokenType : VerificationType = token_type match {
    case 'e' => UserEmailVerification
    case 'p' => PasswordChange
  }
}

abstract class VerificationType (val message : String)
case object UserEmailVerification extends VerificationType("E-Mail Address")
case object PasswordChange        extends VerificationType("Password Change")