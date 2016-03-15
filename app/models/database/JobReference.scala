package models.database

import javax.inject.{Singleton, Inject}

import models.misc.RandomString
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by astephens on 02.03.16.
  */
class JobReferenceTableDef(tag: Tag) extends Table[DBJobRef](tag, "job_reference") {

  def session_id     = column[String]("session_id")
  def user_id        = column[Long]("users_user_id")
  def main_id        = column[Long]("jobs_main_id")
  def referral_link  = column[String]("referral_link")
  def description_id = column[Long]("description_id")
  def created_on     = column[Long]("created_on")
  def updated_on     = column[Long]("updated_on")
  def viewed_on      = column[Long]("viewed_on")
  override def * = (session_id, user_id, main_id, referral_link) <> (DBJobRef.tupled, DBJobRef.unapply)
}

@Singleton
class JobReference @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider : DatabaseConfigProvider,
                                                          jobDB            : models.database.Jobs) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val jobReferences = TableQuery[JobReferenceTableDef] //Loads the table definition for the JobReference Table

  /**
    * Updates or adds a Job reference and it's links
    *
    * @param job        Job for which the reference should be made
    * @param session_id Session ID from which the job is sent
    */
  def update(job : DBJob, session_id : String) : DBJobRef = {
    val main_id   : Long = jobDB.update(job)
    val jobRefSeq : Seq[DBJobRef] = get(main_id, job.user_id)
    jobRefSeq.headOption match {
      case Some(jobRef) =>
        Await.result(dbConfig.db.run(jobReferences.filter(_.main_id === main_id).update(jobRef)), Duration.Inf)
        jobRef
      case None         =>
        val jobRef = new DBJobRef(session_id, job.user_id, main_id, RandomString.randomAlphaString(15))
        Await.result(dbConfig.db.run(jobReferences += jobRef), Duration.Inf)
        jobRef
    }
  }

  /**
    * Find Jobs by main_id in the Job Reference Table
    * @param main_id Main ID of a job
    * @return
    */
  def get(main_id : Long) : Seq[DBJobRef] = {
    Await.result(dbConfig.db.run(jobReferences.filter(_.main_id === main_id).result), Duration.Inf)
  }

  /**
    * Find Jobs by main_id and user_id in the Job Reference Table
    * @param main_id main_id of a job
    * @param user_id user_id of an user
    * @return
    */
  def get(main_id : Long, user_id : Long) : Seq[DBJobRef] = {
    Await.result(dbConfig.db.run(
      jobReferences.filter(_.main_id === main_id).filter(_.user_id === user_id).result),
      Duration.Inf
    )
  }

  /**
    * Find Jobs my session_id in the Job Reference Table
    * @param session_id session_id of an user
    * @return
    */
  def get(session_id : String) : Seq[DBJobRef] = {
    Await.result(dbConfig.db.run(jobReferences.filter(_.session_id === session_id).result), Duration.Inf)
  }

  /**
    * Find Jobs by main_id and user_id in the Job Reference Table
    * @param main_id main_id of a job
    * @param session_id session_id of an user
    * @return
    */
  def get(main_id : Long, session_id : String) : Seq[DBJobRef] = {
    Await.result(dbConfig.db.run(
      jobReferences.filter(_.main_id === main_id).filter(_.session_id === session_id).result),
      Duration.Inf
    )
  }

  /**
    * Deletes a single JobRef in the Reference database
    * @param jobRef job reference to delete
    * @return
    */
  def delete(jobRef : DBJobRef) : Option[DBJobRef] = {
    val jobOption = get(jobRef.main_id, jobRef.session_id).headOption
    jobOption match {
      case Some(jobRef) =>
        dbConfig.db.run(jobReferences.filter(_.main_id === jobRef.main_id).delete)
        Some(jobRef)
      case None =>
        None : Option[DBJobRef]
    }
  }

  /**
    * Deletes all instances of JobReferences linking to the main_id
    * @param main_id main_id of the Job to which the references lead
    * @return
    */
  def delete(main_id : Long) = {
    dbConfig.db.run(jobReferences.filter(_.main_id === main_id).delete)
  }
}

case class DBJobRef(val session_id    : String, // The Session ID from which the Reference was made
                    val user_id       : Long,   // ID of the user who should get the reference
                    val main_id       : Long,   // The main ID of the Job
                    val referral_link : String) // User is the main user