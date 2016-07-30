package models.mailing

import models.database.MongoDBUser

/**
  * Created by astephens on 24.05.16.
  */
/**
  * Template trait to the Mailing
  */
sealed trait MailTemplate {
  // Subject of the Email
  def subject = "Generic Information"
  // Text version of the Email
  def bodyText (user : MongoDBUser) : String
  // HTML version of the Email
  def bodyHtml (user : MongoDBUser) : String

  def bodyHtmlTemplate(subject : String, content : String) =
      s"""
         |<html>
         |  <body>
         |    <p>$subject</p>
         |    <p>$content</p>
         |  </body>
         |</html>
    """.stripMargin
}

class NewUserWelcomeMail(token : String) extends MailTemplate {
  override def subject = "Bioinformatics Toolkit - Verification"

  def bodyText (user : MongoDBUser) = {
    s"""Welcome ${user.nameLogin},
       |Your Registration was successful. Please take a moment and verify that this is indeed your E-Mail account.
       |To do this, visit
       |http://olt:7550/verification/${user.nameLogin}/$token
       |Your Toolkit Team
     """.stripMargin
  }

  def bodyHtml(user : MongoDBUser) = {
    super.bodyHtmlTemplate(
      s"""Welcome ${user.nameLogin},""".stripMargin,
      s"""Your Registration was successful. Please take a moment and verify that this is indeed your E-Mail account.
       |To do this, click <a href=\"http://olt:7550/verification/${user.nameLogin}/$token\">here</a>
       |or copy this URL and visit this page in your browser:
       |http://olt:7550/verification/${user.nameLogin}/$token
       |Your Toolkit Team
     """.stripMargin
    )
  }
}