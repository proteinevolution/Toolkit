package models.mailing

import models.database.User

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
  def bodyText (user : User) : String
  // HTML version of the Email
  def bodyHtml (user : User) : String

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

final case class NewUserWelcomeMail(token : String) extends MailTemplate {
  override def subject = "Bioinformatics Toolkit - Verification"

  def bodyText (user : User) = {
    s"""Welcome ${user.getUserData.nameLogin},
       |Your Registration was successful. Please take a moment and verify that this is indeed your E-Mail account.
       |To do this, visit
       |http://olt:7550/verification/${user.getUserData.nameLogin}/$token
       |Your Toolkit Team
     """.stripMargin
  }

  def bodyHtml(user : User) = {
    super.bodyHtmlTemplate(
      s"""Welcome ${user.getUserData.nameLogin},""".stripMargin,
      s"""Your Registration was successful. Please take a moment and verify that this is indeed your E-Mail account.
       |To do this, click <a href=\"http://olt:7550/verification/${user.getUserData.nameLogin}/$token\">here</a>
       |or copy this URL and visit this page in your browser:
       |http://olt:7550/verification/${user.getUserData.nameLogin}/$token
       |Your Toolkit Team
     """.stripMargin
    )
  }
}