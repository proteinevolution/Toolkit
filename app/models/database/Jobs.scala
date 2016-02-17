package models.database

import models.jobs.DBJob
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


class JobTableDef(tag: Tag) extends Table[DBJob](tag, "job") {

  def id = column[Long]("id", O.PrimaryKey,O.AutoInc)
  def jobid = column[Long]("id", O.PrimaryKey,O.AutoInc)


  override def * =
    (id, jobid) <>(DBJob.tupled, DBJob.unapply)
}



object Jobs {

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  val jobs = TableQuery[JobTableDef]

  def add(job: DBJob): Future[String] = {
    dbConfig.db.run(jobs += job).map(res => "Job successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
  }

  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(jobs.filter(_.id === id).delete)
  }

  def get(id: Long): Future[Option[DBJob]] = {
    dbConfig.db.run(jobs.filter(_.id === id).result.headOption)
  }

  def listAll: Future[Seq[DBJob]] = {
    dbConfig.db.run(jobs.result)
  }
}