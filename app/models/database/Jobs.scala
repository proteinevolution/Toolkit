package models.database

import javax.inject.{Singleton, Inject}

import models.jobs._
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.duration.Duration
import scala.concurrent.Await



class JobsTableDef(tag: Tag) extends Table[DBJob](tag, "jobs") {

  implicit val jobStateChar = MappedColumnType.base[JobState, Char](
    {
      case PartiallyPrepared => '0'
      case Prepared =>          'p'
      case Queued =>            'q'
      case Running =>           'r'
      case Error =>             'e'
      case Done =>              'd'
      case Submitted =>         'i'
    }, {
      case '0' => PartiallyPrepared
      case 'p' => Prepared
      case 'q' => Queued
      case 'r' => Running
      case 'e' => Error
      case 'd' => Done
      case 'i' => Submitted
    }
  )

  def main_id   = column[Long]("main_id", O.PrimaryKey,O.AutoInc)
  def job_id    = column[String]("job_id")
  def user_id   = column[Long]("user_id")
  def job_state = column[JobState]("status")
  def toolname  = column[String]("tool")

  override def * = (main_id.?, job_id, user_id, job_state, toolname) <> (DBJob.tupled, DBJob.unapply)
}

@Singleton
class Jobs @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider: DatabaseConfigProvider) {


  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val jobs = TableQuery[JobsTableDef]                //Loads the table definition for the Job Table

  // Defines that adding the Job with a query will return the new auto-incremented main_id
  val addQuery = jobs returning jobs.map(_.main_id)
  val deleteQuery = jobs returning jobs.map(_.main_id)
  val updateQuery = jobs returning jobs.map(_.main_id)
  val getQuery = jobs returning jobs.map(_.main_id)
  //val likeQuery = jobs  jobs.map(_.job_id)

  /**
    * Finds Jobs based on a partial job_id
    * @param user_id
    * @param job_id_part
    * @return
    */
  def suggestJobID(user_id: Long, job_id_part : String) : Seq[DBJob] = {
    Await.result(dbConfig.db.run(jobs.filter(_.user_id === user_id).filter(_.job_id.startsWith(job_id_part)).result), Duration.Inf)
  }

  /**
    * Removes the first Job from the Database permanently using the job_id and the user_id
    * @param user_id
    * @param job_id
    * @return
    */
  def delete(user_id : Long, job_id : String) : Option[DBJob] = {
    val dbJobOption = get(user_id, job_id).headOption
    dbJobOption match {
      case Some(dbJob) =>
        dbConfig.db.run(jobs.filter(_.main_id === dbJob.main_id).delete)
      case None =>
    }
    dbJobOption
  }

  /**
    * Removes a Job from the Database permanently using the main_id
    * @param main_id
    * @return
    */
  def delete(main_id : Long) : Option[DBJob] = {
    val dbJobOption = get(main_id).headOption
    dbJobOption match {
      case Some(dbJob) =>
        dbConfig.db.run(jobs.filter (_.main_id === main_id).delete)
      case None =>
    }
    dbJobOption
  }

  /**
    * Find Jobs in the Database with the matching filter
    * @param main_id ID for primary key of Jobs
    * @return
    */
  def get(main_id: Long) : Option[DBJob] = {
    Await.result(dbConfig.db.run(jobs.filter(_.main_id === main_id).result), Duration.Inf).headOption
  }

  /**
    * Find Jobs in the Database with the matching filter
    * @param user_id user ID of the user who first created the job
    * @param job_id Job ID (Name) of the job
    * @return
    */
  def get(user_id: Long, job_id : String) : Seq[DBJob] = {
    Await.result(dbConfig.db.run(jobs.filter(_.user_id === user_id).filter(_.job_id === job_id).result), Duration.Inf)
  }

  /**
    * Find all of the Jobs a User owns
    * @param user_id user_id of the user who first created (and owns) the job
    * @return
    */
  def getJobsForUser(user_id : Long) : Seq[DBJob] = {
    Await.result(dbConfig.db.run(jobs.filter(_.user_id === user_id).result), Duration.Inf)
  }

  /**
    * Updates a Job in the database
    * @param job
    * @return
    */
  def update(dbJob: DBJob) : Option[DBJob] = {
    dbJob.main_id match {
      // This is an update
      case Some(main_id) =>
        Await.result(dbConfig.db.run(jobs.filter(_.main_id === main_id).update(dbJob)), Duration.Inf)
        get(main_id)
      // This is a creation
      case None =>
        val main_id : Long = Await.result(dbConfig.db.run(addQuery += dbJob), Duration.Inf)
        get(main_id)
    }
  }

  /**
    * Returns the main_id of a job by passing the user_id and job_id
    * @param user_id
    * @param job_id
    * @return
    */
  def getMainID(user_id : Long, job_id : String) : Option[Long] = {
    get(user_id, job_id).head.main_id
  }

  /**
    * Returns the main_id of a user job by passing the job itself
    * @param job
    * @return
    */
  def getMainID(job : UserJob) : Option[Long] = {
    get(job.user_id, job.job_id).head.main_id
  }
}

//Job Class used for database storage
case class DBJob(    main_id   : Option[Long],  // Main ID of the Job (primary key, auto inc)
                 val job_id    : String,        // Job ID/Name of the Job (non unique)
                 val user_id   : Long,          // User ID of the user who created the job
                 val job_state : JobState,      // State of the Job
                     toolname  : String)        // Name of the Tool
