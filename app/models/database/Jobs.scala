package models.database

import javax.inject.{Singleton, Inject}

import models.jobs.DBJob
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class JobsTableDef(tag: Tag) extends Table[DBJob](tag, "jobs") {

  def main_id   = column[Long]("main_id", O.PrimaryKey,O.AutoInc)
  def job_id    = column[String]("job_id")
  def user_id   = column[String]("user_id")
  def status    = column[Char]("status")
  def toolname  = column[String]("tool")

  override def * = (job_id, user_id, toolname) <> (DBJob.tupled, DBJob.unapply)
}

@Singleton
class Jobs @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val jobs = TableQuery[JobsTableDef]                //Loads the table definition for the Job Table

  // Maps user_id and job_id to the corresponding main_id
  val userJobMapping = new collection.mutable.HashMap[(String, String), Future[Long]]()

  // Defines that adding the Job with a query will return the new auto-incremented main_id
  val addQuery = jobs returning jobs.map(_.main_id)
  val deleteQuery = jobs returning jobs.map(_.main_id)


  def delete(user_id : String, job_id : String) : Future[Long] = {

    userJobMapping.remove(user_id -> job_id).get.map { main_id =>

      dbConfig.db.run(jobs.filter(_.main_id === main_id).delete)
      main_id
    }
  }

  /*
  def get(main_id: Long): Future[Option[DBJob]] = {
    dbConfig.db.run(jobs.filter(_.main_id === main_id).result.headOption)
  }

  def listAll: Future[Seq[DBJob]] = {
    dbConfig.db.run(jobs.result)
  }
  */

  def add(job: DBJob) : Future[Long] = {

    val res: Future[Long] = dbConfig.db.run(addQuery += job)
    userJobMapping.put(job.user_id -> job.job_id, res)
    res
  }
}
