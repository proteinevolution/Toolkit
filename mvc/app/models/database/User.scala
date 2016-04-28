package models.database

import javax.inject.{Inject, Singleton}

import models.users.DBUser
import play.api.db.slick.DatabaseConfigProvider
import play.db.NamedDatabase
import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

/**
  * Class containing User credentials with a link to the database
  *
  * @param tag
  */

class UsersTableDef(tag: Tag) extends Table[User](tag, "jobs") {

  def user_id         = column[Long]("user_id", O.PrimaryKey,O.AutoInc)
  def user_name_first = column[String]("user_name_first")
  def user_name_last  = column[String]("user_name_last")
  def password        = column[String]("password")
  def email           = column[String]("email")
  def created_on      = column[Long]("created_on")
  def updated_on      = column[Long]("updated_on")

  override def * = (user_id.?, user_name_last, user_name_first, password, email) <>(User.tupled, User.unapply)
}

@Singleton
class Users @Inject()(@NamedDatabase("tkplay_dev") dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val users = TableQuery[UsersTableDef]

  // Defines that adding the Job with a query will return the new auto-incremented main_id
  val addQuery = users returning users.map(_.user_id)

  /**
    * Adds or Updates a User in the database
    * @param user user to be updated
    * @return
    */
  def update(user: User) : Option[User] = {
    user.user_id match {
      case Some(user_id) =>
        Await.result(dbConfig.db.run(users.filter(_.user_id === user_id).update(user)), Duration.Inf)
        get(user_id)
      case None =>
        val user_id : Long = Await.result(dbConfig.db.run(addQuery += user), Duration.Inf)
        get(user_id)
    }
  }

  /**
    * Find users in the Database with the matching filter
    * @param user_id ID for primary key of Jobs
    * @return
    */
  def get (user_id : Long) : Option[User] = {
    Await.result(dbConfig.db.run(users.filter(_.user_id === user_id).result), Duration.Inf).headOption
  }

  def add(user : User) : Future[Long] = {
    val res: Future[Long] = dbConfig.db.run(addQuery += user)
    res
  }


  /**
    * Find users in the Database with the matching filter
    * @param email the email of the User
    * @return
    */
  def get (email : String) : Option[User] = {
    Await.result(dbConfig.db.run(users.filter(_.email === email).result), Duration.Inf).headOption
  }
}


//User Class used for database storage
case class User(val user_id         : Option[Long],
                val user_name_last  : String,
                val user_name_first : String,
                val password        : String,
                val email           : String)