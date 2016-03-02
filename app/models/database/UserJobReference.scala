package models.database

import models.users.DBUserJob

import javax.inject.{Singleton, Inject}

import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

/**
  * Created by astephens on 02.03.16.
  */
class UserJobTableDef (tag: Tag) extends Table[DBUserJob](tag, "sessionjob") {

  def user_id   = column[Long]("user_id")
  def job_id    = column[String]("job_id")
  def main_user = column[Boolean]("main_user")

  override def * = (user_id, job_id, main_user) <> (DBUserJob.tupled, DBUserJob.unapply)
}

@Singleton
class UserJob @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val jobs = TableQuery[UserJobTableDef]       //Loads the table definition for the Job Table
}