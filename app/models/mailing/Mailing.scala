package models.mailing

import models.database.jobs._
import models.database.users.User
import play.api.libs.mailer.{MailerClient, Email}
import modules.tel.TEL

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

  def bodyHtmlTemplate(subject : String, content : String) : String =
      s"""
         |<html>
         |  <body>
         |    <p>$subject</p>
         |    <p>$content</p>
         |  </body>
         |</html>
    """.stripMargin
}

case class NewUserWelcomeMail (userParam : User, token : String) extends MailTemplate {
  override def subject = "Account Verification - Bioinformatics Toolkit"

  val user : User = userParam

  val bodyText : String = {
    s"""Welcome ${user.getUserData.nameLogin},
       |Your Registration was successful. Please take a moment and verify that this is indeed your E-Mail account.
       |To do this, visit
       |http://${TEL.hostname}:${TEL.port}/verification/${user.getUserData.eMail}/$token
       |Your Toolkit Team
     """.stripMargin
  }

  val bodyHtml : String = {
    super.bodyHtmlTemplate(
      s"""Welcome ${user.getUserData.nameLogin},<br />""".stripMargin,
      s"""Your Registration was successful. Please take a moment and verify that this is indeed your E-Mail account.<br />
       |To do this, click <a href=\"http://${TEL.hostname}:${TEL.port}/verification/${user.getUserData.eMail}/$token\">here</a><br />
       |or copy this URL and visit this page in your browser:<br />
       |http://${TEL.hostname}:${TEL.port}/verification/${user.getUserData.eMail}/$token<br />
       |Your Toolkit Team
     """.stripMargin
    )
  }
}

case class ChangePasswordMail (userParam : User, token : String) extends MailTemplate {
  override def subject = "Password Verification - Bioinformatics Toolkit"

  val user : User = userParam

  val bodyText : String = {
    s"""Hello ${user.getUserData.nameLogin},
        |You requested a password change.
        |To complete the process, visit
        |http://${TEL.hostname}:${TEL.port}/verification/${user.getUserData.eMail}/$token
        |If You did not request this, then your account has been used by someone else.
        |Log in and change the password yourself to ensure that this other Person can no longer access your account.
        |Your Toolkit Team
     """.stripMargin
  }

  val bodyHtml : String = {
    super.bodyHtmlTemplate(
      s"""Hello ${user.getUserData.nameLogin},<br />""".stripMargin,
      s"""You requested a password change.<br />
          |To complete the process, click <a href=\"http://${TEL.hostname}:${TEL.port}/verification/${user.getUserData.eMail}/$token\">here</a><br />
          |or copy this URL and visit this page in your browser:<br />
          |http://${TEL.hostname}:${TEL.port}/verification/${user.getUserData.eMail}/$token<br />
          |If You did not request this, then your account has been used by someone else.<br />
          |Log in and change the password yourself to ensure that this other Person can no longer access your account.<br />
          |Your Toolkit Team<br />
     """.stripMargin
    )
  }
}

case class ResetPasswordMail (userParam : User, token : String) extends MailTemplate {
  override def subject = "Password Verification - Bioinformatics Toolkit"

  val user : User = userParam

  val bodyText : String = {
    s"""Hello ${user.getUserData.nameLogin},
        |You requested to reset your password and set a new one.
        |To complete the process, visit
        |http://${TEL.hostname}:${TEL.port}/verification/${user.getUserData.eMail}/$token
        |If You did not request this, then someone may have tried to log into your account.
        |Your Toolkit Team
     """.stripMargin
  }

  val bodyHtml : String = {
    super.bodyHtmlTemplate(
      s"""Hello ${user.getUserData.nameLogin},<br />""".stripMargin,
      s"""You requested to reset your password and set a new one.<br />
          |To complete the process, visit <a href=\"http://${TEL.hostname}:${TEL.port}/verification/${user.getUserData.eMail}/$token\">here</a><br />
          |or copy this URL and visit this page in your browser:<br />
          |http://${TEL.hostname}:${TEL.port}/verification/${user.getUserData.eMail}/$token<br />
          |If You did not request this, then your account has been used by someone else.<br />
          |Log in and change the password yourself to ensure that this other Person can no longer access your account.<br />
          |Your Toolkit Team
     """.stripMargin
    )
  }
}

case class PasswordChangedMail (userParam : User) extends MailTemplate {
  override def subject = "Password Changed - Bioinformatics Toolkit"

  val user : User = userParam

  val bodyText : String = {
    s"""Hello ${user.getUserData.nameLogin},
        |As requested your password has been changed. You can do this at any time in your user profile.
        |If You did not request this, then someone else may have changed your password.
        |Your Toolkit Team
     """.stripMargin
  }

  val bodyHtml : String = {
    super.bodyHtmlTemplate(
      s"""Hello ${user.getUserData.nameLogin},<br />""".stripMargin,
      s"""You requested to reset your password and set a new one.<br />
          |As requested your password has been changed. You can do this at any time in your user profile.<br />
          |If You did not request this, then someone else may have changed your password.<br />
          |Your Toolkit Team
     """.stripMargin
    )
  }
}

case class JobFinishedMail (userParam : User, job : Job) extends MailTemplate {
  override def subject = s"""Job ${job.jobID} finished running - Bioinformatics Toolkit""".stripMargin

  val user : User = userParam

  def statusMessage : String = {
    job.status match {
      case Done =>  "Your job has finished sucessfully. You can now look at the results."
      case Error => "Your job has failed. Please check all parameters and see if you find any issues."
      case _ =>     "Has changed state"
    }
  }

  val bodyText : String = {
    s"""Hello ${user.getUserData.nameLogin},
        |$statusMessage
        |You can view it at any time at http://${TEL.hostname}:${TEL.port}/jobs/${job.jobID}
        |Your Toolkit Team
     """.stripMargin
  }

  val bodyHtml : String = {
    super.bodyHtmlTemplate(
      s"""Hello ${user.getUserData.nameLogin},<br />""".stripMargin,
      s"""$statusMessage
          |You can view it at any time <a href=\"http://${TEL.hostname}:${TEL.port}/jobs/${job.jobID}>here</a>
          |or go to http://${TEL.hostname}:${TEL.port}/jobs/${job.jobID} in your browser<br />
          |Your Toolkit Team
     """.stripMargin
    )
  }
}