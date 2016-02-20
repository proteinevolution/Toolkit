package models.database

import models.jobs.DBJob
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class JobTableDef(tag: Tag) extends Table[DBJob](tag, "jobs") {

  def main_id = column[Long]("main_id", O.PrimaryKey,O.AutoInc)
  def job_id = column[String]("job_id")
  def user_id = column[Long]("user_id")


  override def * = (job_id, user_id) <>(DBJob.tupled, DBJob.unapply)
}


object Jobs {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val jobs = TableQuery[JobTableDef]

  def add(job: DBJob): Future[String] = {
    dbConfig.db.run(jobs += job).map(res => "Job successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(main_id: Long): Future[Int] = {
    dbConfig.db.run(jobs.filter(_.main_id === main_id).delete)
  }

  def get(main_id: Long): Future[Option[DBJob]] = {
    dbConfig.db.run(jobs.filter(_.main_id === main_id).result.headOption)
  }

  def listAll: Future[Seq[DBJob]] = {
    dbConfig.db.run(jobs.result)
  }
}