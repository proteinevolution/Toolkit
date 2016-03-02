package models.database

import javax.inject.{Singleton, Inject}

import models.sessions.DBSession
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.Future


class SessionsTableDef(tag: Tag) extends Table[DBSession](tag, "sessions") {

  def session_id   = column[String]("session_id", O.PrimaryKey)
  def user_id      = column[Long]("users_user_id")
  def session_ip   = column[String]("session_ip")

  override def * = (session_id, user_id, session_ip) <> (DBSession.tupled, DBSession.unapply)
}

@Singleton
class Sessions @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val sessions = TableQuery[SessionsTableDef]       //Loads the table definition for the Session Table
}
