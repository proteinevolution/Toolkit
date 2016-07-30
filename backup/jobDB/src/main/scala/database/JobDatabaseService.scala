package database


import javax.inject.{Inject, Singleton}

import models.jobs._
import play.api.Logger
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._



class JobsTableDef(tag: Tag) extends Table[DBJob](tag, "jobs") {

  implicit val jobStateChar = MappedColumnType.base[JobState, Char]({
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

  def jobID     = column[Int]("job_id", O.PrimaryKey)
  def sessionID = column[String]("session_id")
  def job_state = column[JobState]("status")
  def toolname  = column[String]("tool")

  override def * = (jobID, sessionID, job_state, toolname) <> (DBJob.tupled, DBJob.unapply)
}

@Singleton
class JobDatabaseService  {

  val db =  Database.forConfig("jobs")
  val jobs = TableQuery[JobsTableDef]                //Loads the table definition for the Job Table

  /*
  def suggestJobID(user_id: Long, job_id_part : String) : Seq[DBJob] = {
    Await.result(db.run(jobs.filter(_.user_id === user_id).filter(_.job_id.startsWith(job_id_part)).result), Duration.Inf)
  }
*/


  /**
    * Updates a Job in the database
    * @return
    */
  def update(dbJob : DBJob)  = {

    Logger.info("Update Database")
    db.run(jobs.insertOrUpdate(dbJob))
  }



  /**
    * Returns the main_id of a user job by passing the job itself
    * @return
    */
  /*
  def getMainID(job : UserJob) : Option[Long] = {
    get(job.sessionID, job.jobID).head.main_id
  } */
}


//Job Class used for database storage
case class DBJob(    jobID     : Int,        // Job ID/Name of the Job (non unique)
                     sessionID   : String,          // User ID of the user who created the job
                     job_state : JobState,      // State of the Job
                     toolname  : String)        // Name of the Tool
