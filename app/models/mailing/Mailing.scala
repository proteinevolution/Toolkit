package models.mailing

import models.database.jobs._
import models.database.users.User
import play.api.libs.mailer.{Email, MailerClient}
import modules.tel.TEL
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

/**
  * Created by astephens on 24.05.16.
  */

object MailTemplate {
  // Date time format for the "deleting your account on" mail
  val dtf = DateTimeFormat.forPattern("EEEE, dd.MM.yyyy")
}
/**
  * Template trait to the Mail object
  */
sealed trait MailTemplate {
  // User to whom the eMail should be sent
  val user: User
  // Subject of the Email
  def subject = "Generic Information"

  // Text version of the Email
  val bodyText: String

  // HTML version of the Email
  val bodyHtml: String

  // Send the email
  def send(implicit mailerClient: MailerClient) {
    val email = Email(
      subject,
      "Toolkit Team <mpi-toolkit@tuebingen.mpg.de>",
      Seq(user.getUserData.nameLogin + " <" + user.getUserData.eMail + ">"),
      attachments = Seq(),
      bodyText = Some(this.bodyText), // Text version of the E-Mail in case the User has no HTML E-Mail client
      bodyHtml = Some(this.bodyHtml) // HTML formatted E-Mail content
    )
    mailerClient.send(email)
  }

  def bodyHtmlTemplate(subject: String, content: String): String =
    s"""
         |<html>
         |  <body>
         |    <p>$subject</p>
         |    <p>$content</p>
         |  </body>
         |</html>
    """.stripMargin

  val origin: String = TEL.hostname match {

    case "olt" => s"http://${TEL.hostname}:${TEL.port}"
    case _     => s"https://toolkit.tuebingen.mpg.de"

  }

}

case class NewUserWelcomeMail(userParam: User, token: String) extends MailTemplate {
  override def subject = "Account Verification - Bioinformatics Toolkit"

  val user: User = userParam

  val bodyText: String = {
    s"""Welcome ${user.getUserData.nameLogin},
       |your registration was successful. Please take a moment and verify that this is indeed your E-Mail account.
       |To do this, visit
       |$origin/verification/${user.getUserData.nameLogin}/$token
       |Your Toolkit Team
     """.stripMargin
  }

  val bodyHtml: String = {
    super.bodyHtmlTemplate(
      s"""Welcome ${user.getUserData.nameLogin},<br />""".stripMargin,
      s"""your registration was successful. Please take a moment and verify that this is indeed your E-Mail account.<br />
       |To do this, click <a href=\"$origin/verification/${user.getUserData.nameLogin}/$token\">here</a><br />
       |or copy this URL and visit this page in your browser:<br />
       |$origin/verification/${user.getUserData.nameLogin}/$token<br /><br />
       |Your Toolkit Team
     """.stripMargin
    )
  }
}

case class ChangePasswordMail(userParam: User, token: String) extends MailTemplate {
  override def subject = "Password Verification - Bioinformatics Toolkit"

  val user: User = userParam

  val bodyText: String = {
    s"""Dear ${user.getUserData.nameLogin},
        |you requested a password change.
        |To complete the process, visit
        |$origin/verification/${user.getUserData.nameLogin}/$token
        |If you did not request this, then your account has been used by someone else.
        |Log in and change the password yourself to ensure that this other person can no longer access your account.
        |Your Toolkit Team
     """.stripMargin
  }

  val bodyHtml: String = {
    super.bodyHtmlTemplate(
      s"""Dear ${user.getUserData.nameLogin},<br />""".stripMargin,
      s"""you requested a password change.<br />
          |To complete the process, click <a href=\"$origin/verification/${user.getUserData.nameLogin}/$token\">here</a><br />
          |or copy this URL and visit this page in your browser:<br />
          |$origin/verification/${user.getUserData.nameLogin}/$token<br />
          |If you did not request this, then your account has been used by someone else.<br />
          |Log in and change the password yourself to ensure that this other person can no longer access your account.<br /><br />
          |Your Toolkit Team<br />
     """.stripMargin
    )
  }
}

case class ResetPasswordMail(userParam: User, token: String) extends MailTemplate {
  override def subject = "Password Verification - Bioinformatics Toolkit"

  val user: User = userParam

  val bodyText: String = {
    s"""Dear ${user.getUserData.nameLogin},
        |you requested to reset your password and set a new one.
        |To complete the process, visit
        |$origin/verification/${user.getUserData.nameLogin}/$token
        |If you did not request this, then someone may have tried to log into your account.
        |Your Toolkit Team
     """.stripMargin
  }

  val bodyHtml: String = {
    super.bodyHtmlTemplate(
      s"""Dear ${user.getUserData.nameLogin},<br />""".stripMargin,
      s"""you requested to reset your password and set a new one.<br />
          |To complete the process, visit <a href=\"$origin/verification/${user.getUserData.nameLogin}/$token\">here</a><br />
          |or copy this URL and visit this page in your browser:<br />
          |$origin/verification/${user.getUserData.nameLogin}/$token<br />
          |If you did not request this, then your account has been used by someone else.<br />
          |Log in and change the password yourself to ensure that this other person can no longer access your account.<br /><br />
          |Your Toolkit Team
     """.stripMargin
    )
  }
}

case class PasswordChangedMail(userParam: User) extends MailTemplate {
  override def subject = "Password Changed - Bioinformatics Toolkit"

  val user: User = userParam

  val bodyText: String = {
    s"""Dear ${user.getUserData.nameLogin},
        |as requested your password has been changed. You can do this at any time in your user profile.
        |If You did not request this, then someone else may have changed your password.
        |Your Toolkit Team
     """.stripMargin
  }

  val bodyHtml: String = {
    super.bodyHtmlTemplate(
      s"""Dear ${user.getUserData.nameLogin},<br />""".stripMargin,
      s"""you requested to reset your password and set a new one.<br />
          |As requested your password has been changed. You can do this at any time in your user profile.<br />
          |If you did not request this, then someone else may have changed your password.<br /><br />
          |Your Toolkit Team
     """.stripMargin
    )
  }
}

case class JobFinishedMail(userParam: User, job: Job) extends MailTemplate {
  override def subject: String = s"""Job ${job.jobID} finished running - Bioinformatics Toolkit""".stripMargin

  val user: User = userParam

  def statusMessage: String = {
    job.status match {
      case Done  => "your job has finished successfully. You can now look at the results."
      case Error => "your job has failed. Please check all parameters and see if you find any issues."
      case _     => "your job has changed state."
    }
  }

  val bodyText: String = {
    s"""Dear ${user.getUserData.nameLogin},
        |$statusMessage
        |you can view it at any time at $origin/jobs/${job.jobID}
        |Your Toolkit Team
     """.stripMargin
  }

  val bodyHtml: String = {
    super.bodyHtmlTemplate(
      s"""Dear ${user.getUserData.nameLogin},<br />""".stripMargin,
      s"""$statusMessage
          |You can view it at any time <a href=\"$origin/jobs/${job.jobID}>here</a>
          |or go to $origin/jobs/${job.jobID} in your browser<br /><br />
          |Your Toolkit Team
     """.stripMargin
    )
  }
}


case class OldAccountEmail(userParam: User, deletionMonths : Int) extends MailTemplate {
  override def subject = "Old Account - Bioinformatics Toolkit"

  val user: User = userParam

  val bodyText: String = {
    s"""Dear ${user.getUserData.nameLogin},
       |we have noticed, that you have not logged in since ${user.dateLastLogin.map(d => MailTemplate.dtf.print(d)).getOrElse("[date not supplied]")}.
       |To keep our system running smoothly and to keep the data we collect from our users to a minimum,
       |we delete old user accounts.
       |This is why Your account will be deleted on ${user.dateLastLogin.map(d => MailTemplate.dtf.print(d.plusMonths(deletionMonths))).getOrElse("[date not supplied]")}.
       |To prevent this from happening, just log in to your account to let us know,
       |that You are still interested in our services.
       |
       |Your Toolkit Team
       |
       |$origin
     """.stripMargin
  }

  val bodyHtml: String = {
    super.bodyHtmlTemplate(
      s"""Dear ${user.getUserData.nameLogin},<br />""".stripMargin,
      s"""we have noticed, that you have not logged in since ${user.dateLastLogin.map(d => MailTemplate.dtf.print(d)).getOrElse("[date not supplied]")}.<br />
         |To keep our system running smoothly and to keep the data we collect from our users to a minimum,
         |we delete old user accounts.<br />
         |This is why Your account will be deleted on<br />
         |${user.dateLastLogin.map(d => MailTemplate.dtf.print(d.plusMonths(deletionMonths))).getOrElse("[date not supplied]")}.<br />
         |To prevent this from happening, just log in to your account to let us know,
         |that You are still interested in our services.<br /><br />
         |<a href="$origin">Your Toolkit Team</a>
     """.stripMargin
    )
  }
}