package models.mailing

import models.database.User
import play.api.libs.mailer.{MailerClient, Email}

/**
  * Created by astephens on 24.05.16.
  */




/**
  * Template trait to the Mail object
  */
sealed trait MailTemplate {
  // User to whom the eMail should be sent
  val user : User
  // Subject of the Email
  def subject = "Generic Information"

  // Text version of the Email
  val bodyText : String

  // HTML version of the Email
  val bodyHtml : String

  // Send the email
  def send(implicit mailerClient : MailerClient) {
    val email = Email(
      subject,
      "Toolkit Team <toolkitmpg@gmail.com>",
      Seq(user.getUserData.nameLogin + " <" + user.getUserData.eMail + ">"),
      attachments = Seq(),
      bodyText = Some(this.bodyText), // Text version of the E-Mail in case the User has no HTML E-Mail client
      bodyHtml = Some(this.bodyHtml)  // HTML formatted E-Mail content
    )
    mailerClient.send(email)
  }

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

case class NewUserWelcomeMail(user : User, token : String) extends MailTemplate {
  override def subject = "Bioinformatics Toolkit - Account Verification"

  val bodyText = {
    s"""Welcome ${user.getUserData.nameLogin},
       |Your Registration was successful. Please take a moment and verify that this is indeed your E-Mail account.
       |To do this, visit
       |http://olt:7550/verification/${user.getUserData.nameLogin}/$token
       |Your Toolkit Team
     """.stripMargin
  }

  val bodyHtml = {
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