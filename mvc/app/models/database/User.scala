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

class UsersTableDef(tag: Tag) extends Table[User](tag, "users") {
  def user_id            = column[Long]("user_id", O.PrimaryKey, O.AutoInc)
  def name_login         = column[String]("name_login")
  def name_first         = column[String]("name_first")
  def name_last          = column[String]("name_last")
  def password           = column[String]("password")
  def email              = column[String]("email")
  def institute          = column[String]("institute")
  def street             = column[String]("street")
  def city               = column[String]("city")
  def country            = column[String]("country")
  def groups             = column[String]("groups")
  def role               = column[String]("role")
  def security_token     = column[String]("security_token")

  def security_token_exp = column[Long]("security_token_exp")
  def created_on         = column[Long]("created_on")
  def updated_on         = column[Long]("updated_on")
  def logged_in_on       = column[Long]("logged_in_on")

  override def * = (user_id.?,
                    name_login,
                    name_last,
                    name_first,
                    password,
                    email,
                    //None,
                    security_token.?,
                    security_token_exp.?) <> (User.tupled, User.unapply)
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

  /**
    * Find users in the Database with the matching filter
    * @param name_login login name of the user
    * @return
    */
  def get (name_login : String) : Option[User] = {
    Await.result(dbConfig.db.run(users.filter(_.name_login === name_login).result), Duration.Inf).headOption
  }
}


//User Data Object used for database storage and interaction
case class User(val user_id            : Option[Long],
                val name_login         : String,
                val name_last          : String,
                val name_first         : String,
                val password           : String,
                val email              : String,
                //val address            : Option[Address] = None,
                val security_token     : Option[String] = None,
                val security_token_exp : Option[Long] = None)

case class Address (val institute      : Option[String] = None,
                    val street         : Option[String] = None,
                    val city           : Option[String] = None,
                    val country        : Option[String] = None,
                    val groups         : Option[String] = None,
                    val role           : Option[String] = None)