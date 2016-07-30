package controllers

import models.database.MongoDBUser
import models.mailing.MailTemplate
import play.api.libs.mailer._
import javax.inject.Inject

/**
  * Created by astephens on 23.05.16.
  */
class Mailing @Inject() (mailerClient: MailerClient) {

  def sendEmail(user : MongoDBUser, template : MailTemplate) {
    //val cid = user.nameLogin TODO What is that supposed to mean ?
    val email = Email(
      template.subject,
      "Toolkit Team <toolkitmpg@gmail.com>",
      Seq(user.nameLogin + " <" + user.nameLogin + ">"),
      attachments = Seq(),
      bodyText = Some(template.bodyText(user)), // Text version of the E-Mail in case the User has no HTML E-Mail client
      bodyHtml = Some(template.bodyHtml(user))  // HTML formatted E-Mail content
    )
    mailerClient.send(email)
  }
}