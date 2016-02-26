package models.database

import javax.inject.{Singleton, Inject}

import models.jobs.DBJob
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.Future


class JobsTableDef(tag: Tag) extends Table[DBJob](tag, "jobs") {

  def main_id   = column[Long]("main_id", O.PrimaryKey,O.AutoInc)
  def job_id    = column[String]("job_id")
  def user_id   = column[Long]("user_id")
  def status    = column[Char]("status")
  def tool_name = column[String]("tool")

  override def * = (job_id, user_id, tool_name) <> (DBJob.tupled, DBJob.unapply)
}

@Singleton
class Jobs @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val jobs = TableQuery[JobsTableDef]

  // Maps user_id and job_id to the corresponding main_id
  val userJobMapping = new collection.mutable.HashMap[(Long, String), Future[Long]]()

  // Defines that adding the Job with a query will return the new auto-incremented main_id
  val addQuery = jobs returning jobs.map(_.main_id)

  /* Not implemented
  def delete(main_id: Long): Future[Int] = {
    dbConfig.db.run(jobs.filter(_.main_id === main_id).delete)
  }

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
