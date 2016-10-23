package controllers

import models.database.User
import models.mailing.MailTemplate
import play.api.libs.mailer._
import javax.inject.Inject

/**
  * Created by astephens on 23.05.16.
  */
class Mailing @Inject() (mailerClient: MailerClient) {

  /**
    * Send a eMail to the user with the given template
    * @param user
    * @param template
    */
  def sendEmail(user : User, template : MailTemplate) {
    val email = Email(
      template.subject,
      "Toolkit Team <toolkitmpg@gmail.com>",
      Seq(user.getUserData.nameLogin + " <" + user.getUserData.eMail + ">"),
      attachments = Seq(),
      bodyText = Some(template.bodyText(user)), // Text version of the E-Mail in case the User has no HTML E-Mail client
      bodyHtml = Some(template.bodyHtml(user))  // HTML formatted E-Mail content
    )
    mailerClient.send(email)
  }
}