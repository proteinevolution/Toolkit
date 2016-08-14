package models.database
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, BSONDocument}

case class UserData(password  : String,         // Password of the User (Hashed)
                    eMail     : String,         // User eMail Address
                    nameFirst : Option[String], // User First Name
                    nameLast  : Option[String], // User Last Name
                    institute : Option[String], // User Workplace
                    street    : Option[String], // User Location
                    city      : Option[String], // User / Institute city
                    country   : Option[String], // 3 Char long Optional String, Country code
                    groups    : Option[String], // Group the User is in
                    roles     : Option[String]) // Position the User is in

object UserData {
  // Constants for the BSON object identifiers
  val EMAIL       = "eMail"
  val PASSWORD    = "password"
  val PASSWORDOLD = "passwordOld"
  val PASSWORDNEW = "passwordNew"
  val NAMEFIRST   = "nameFirst"
  val NAMELAST    = "nameLast"
  val INSTITUTE   = "institute"
  val STREET      = "street"
  val CITY        = "city"
  val COUNTRY     = "country"
  val GROUPS      = "groups"
  val ROLES       = "roles"

  /**
    * Object containing the reader for the Class
    */
  implicit object Reader extends BSONDocumentReader[UserData] {
    def read(bson: BSONDocument): UserData =
      UserData(
        password   = bson.getAs[String](PASSWORD).get,
        eMail      = bson.getAs[String](EMAIL).get,
        nameFirst  = bson.getAs[String](NAMEFIRST),
        nameLast   = bson.getAs[String](NAMELAST),
        institute  = bson.getAs[String](INSTITUTE),
        street     = bson.getAs[String](STREET),
        city       = bson.getAs[String](CITY),
        country    = bson.getAs[String](COUNTRY),
        groups     = bson.getAs[String](GROUPS),
        roles      = bson.getAs[String](ROLES))
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[UserData] {
    def write(userData: UserData): BSONDocument = BSONDocument(
      PASSWORD   -> userData.password,
      EMAIL      -> userData.eMail,
      NAMEFIRST  -> userData.nameFirst,
      NAMELAST   -> userData.nameFirst,
      INSTITUTE  -> userData.institute,
      STREET     -> userData.street,
      CITY       -> userData.city,
      COUNTRY    -> userData.country,
      GROUPS     -> userData.groups,
      ROLES      -> userData.roles)
  }

  /**
    * Helper class for a password change Form Object
    *
    * @param password
    * @param passwordNew
    */
  case class UpdatePasswordForm(password : String, passwordNew : String)

  /**
    * Helpoer class for a profile edit form
    * @param eMail
    * @param nameFirst
    * @param nameLast
    * @param institute
    * @param street
    * @param city
    * @param country
    * @param groups
    * @param roles
    * @param password
    */
  case class EditProfileForm(eMail     : String,
                             nameFirst : Option[String],
                             nameLast  : Option[String],
                             institute : Option[String],
                             street    : Option[String],
                             city      : Option[String],
                             country   : Option[String],
                             groups    : Option[String],
                             roles     : Option[String],
                             password  : String) {
    /**
      * Changes the form object into a user data object
      * @param userData
      * @return
      */
    def toUserData(userData : UserData) = {
      UserData(userData.password, eMail, nameFirst, nameLast, institute, street, city, country, groups, roles)
    }
  }
}