package models.database

import models.sessions.DBSessionJob

import javax.inject.{Singleton, Inject}

import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

/**
  * Created by astephens on 02.03.16.
  */
class SessionJobTableDef (tag: Tag) extends Table[DBSessionJob](tag, "sessionjob") {

    def session_id = column[String]("session_id")
    def job_id     = column[String]("job_id")

    override def * = (session_id, job_id) <> (DBSessionJob.tupled, DBSessionJob.unapply)
}

@Singleton
class SessionJob @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val jobs = TableQuery[SessionJobTableDef]       //Loads the table definition for the Job Table
}