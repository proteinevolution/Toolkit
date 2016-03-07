package models.database

import javax.inject.{Singleton, Inject}

import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

/**
  * Created by astephens on 02.03.16.
  */
class JobReferenceTableDef(tag: Tag) extends Table[JobReference](tag, "jobreference") {

  def session_id    = column[String]("session_id")
  def users_user_id = column[Long]("users_user_id")
  def jobs_main_id  = column[Long]("main_user")
  def referral_link = column[String]("referral_link")
  def session_ip    = column[String]("session_ip")
  def created_on    = column[String]("created_on")
  def updated_on    = column[String]("updated_on")
  def viewed_on     = column[String]("viewed_on")

  override def * = (session_id, jobs_main_id, referral_link) <> (JobReference.tupled, JobReference.unapply)
}

@Singleton
class UserJob @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val jobs = TableQuery[JobReferenceTableDef]       //Loads the table definition for the Job Table

}

case class JobReference (val session_id    : String,   // The Session ID from which the Reference was made
                         val jobs_main_id  : Long,     // The main ID of the Job
                         val referral_link : String) { // User is the main user
}