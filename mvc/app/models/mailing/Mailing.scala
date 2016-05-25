package models.mailing

import models.database.User

/**
  * Created by astephens on 24.05.16.
  */
/**
  * Template class to the Mailing
  */
abstract class MailTemplate {
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

class NewUserWelcomeMail extends MailTemplate {
  override def subject = "Welcome to the Toolkit"

  def bodyText (user : User) = {
    s"""Welcome to the new Toolkit, ${user.name_last}""".stripMargin
  }

  def bodyHtml(user : User) = {
    super.bodyHtmlTemplate(
        "Welcome",
      s"""Welcome to the new Toolkit, ${user.name_last}""".stripMargin
    )
  }
}