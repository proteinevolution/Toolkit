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
      case Prepared => 'p'
      case Queued => 'q'
      case Running => 'r'
      case Error => 'e'
      case Done => 'd'
      case Submitted => 'i'



    },


    {


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
  def user_id   = column[String]("user_id")
  def job_state    = column[JobState]("status")
  def toolname  = column[String]("tool")

  override def * = (job_id, user_id, job_state, toolname) <> (DBJob.tupled, DBJob.unapply)
}

@Singleton
class Jobs @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider: DatabaseConfigProvider) {


  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val jobs = TableQuery[JobsTableDef]                //Loads the table definition for the Job Table

  // Maps user_id and job_id to the corresponding main_id
  val userJobMapping = new collection.mutable.HashMap[(String, String), Long]()

  // Defines that adding the Job with a query will return the new auto-incremented main_id
  val addQuery = jobs returning jobs.map(_.main_id)
  val deleteQuery = jobs returning jobs.map(_.main_id)
  val updateQuery = jobs returning jobs.map(_.main_id)


  def delete(user_id : String, job_id : String) : Option[Long] = {

    val main_id = userJobMapping.remove(user_id -> job_id)
    dbConfig.db.run(jobs.filter(_.main_id === main_id).delete)
    main_id
  }



  def update(job: DBJob) = {

    userJobMapping.get(job.user_id -> job.job_id) match {

      case None =>

        val main_id : Long = Await.result(dbConfig.db.run(addQuery += job), Duration.Inf)
        userJobMapping.put(job.user_id -> job.job_id, main_id)

      case Some(main_id) =>

        dbConfig.db.run(jobs.filter(_.main_id === main_id).update(job))
    }
  }
}



//Job Class used for database storage
case class DBJob(val job_id : String, val user_id : String, val job_state : JobState, toolname : String)
