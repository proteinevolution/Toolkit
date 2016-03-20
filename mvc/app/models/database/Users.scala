package models.database

import javax.inject.{Singleton, Inject}

import models.users.DBUser
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
import scala.concurrent.Future

/**
  * Class creating a link between the database and the users
  * @param tag
  */

class UsersTableDef(tag: Tag) extends Table[DBUser](tag, "jobs") {

  def user_id         = column[Long]("user_id", O.PrimaryKey)
  def user_name_first = column[String]("user_name_first")
  def user_name_last  = column[String]("user_name_last")
  def password        = column[String]("password")
  def email           = column[String]("email")
  def created_on      = column[Long]("created_on")
  def updated_on      = column[Long]("updated_on")

  override def * = (user_id, user_name_last, email) <> (DBUser.tupled, DBUser.unapply)
}

@Singleton
class Users @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val users = TableQuery[UsersTableDef]

  // Defines that adding the Job with a query will return the new auto-incremented main_id
  val addQuery = users returning users.map(_.user_id)

  def add(user: DBUser) : Future[Long] = {
    val res: Future[Long] = dbConfig.db.run(addQuery += user)
    res
  }
}
