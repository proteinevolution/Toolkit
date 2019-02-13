package de.proteinevolution.auth.models

import java.time.ZonedDateTime

import de.proteinevolution.common.models.database.jobs.JobState.{ Done, Error }
import de.proteinevolution.common.models.database.jobs._
import de.proteinevolution.common.models.database.users.User
import de.proteinevolution.common.models.util.ZonedDateTimeHelper
import de.proteinevolution.tel.env.Env
import play.api.libs.mailer.{ Email, MailerClient }

sealed trait MailTemplate {

  val user: User
  def subject = "Generic Information"
  val bodyText: String
  val bodyHtml: String

  def send(implicit mailerClient: MailerClient): Unit = {
    val email = Email(
      subject,
      "Toolkit Team <mpi-toolkit@tuebingen.mpg.de>",
      Seq(user.getUserData.nameLogin + " <" + user.getUserData.eMail + ">"),
      attachments = Seq(),
      bodyText = Some(this.bodyText), // Text version of the E-Mail in case the User has no HTML E-Mail client
      bodyHtml = Some(this.bodyHtml) // HTML formatted E-Mail content
    )
    val _ = mailerClient.send(email)
  }

  def bodyHtmlTemplate(subject: String, content: String): String =
    s"""
         |<!doctype html>
         |<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">
         |
         |<head>
         |  <title>
         |    $subject
         |  </title>
         |  <!--[if !mso]><!-- -->
         |  <meta http-equiv="X-UA-Compatible" content="IE=edge">
         |  <!--<![endif]-->
         |  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
         |  <meta name="viewport" content="width=device-width, initial-scale=1">
         |  <style type="text/css">
         |    #outlook a {
         |      padding: 0;
         |    }
         |
         |    .ReadMsgBody {
         |      width: 100%;
         |    }
         |
         |    .ExternalClass {
         |      width: 100%;
         |    }
         |
         |    .ExternalClass * {
         |      line-height: 100%;
         |    }
         |
         |    body {
         |      margin: 0;
         |      padding: 0;
         |      -webkit-text-size-adjust: 100%;
         |      -ms-text-size-adjust: 100%;
         |    }
         |
         |    table,
         |    td {
         |      border-collapse: collapse;
         |      mso-table-lspace: 0pt;
         |      mso-table-rspace: 0pt;
         |    }
         |
         |    img {
         |      border: 0;
         |      height: auto;
         |      line-height: 100%;
         |      outline: none;
         |      text-decoration: none;
         |      -ms-interpolation-mode: bicubic;
         |    }
         |
         |    p {
         |      display: block;
         |      margin: 13px 0;
         |    }
         |  </style>
         |  <!--[if !mso]><!-->
         |  <style type="text/css">
         |    @media only screen and (max-width:480px) {
         |      @-ms-viewport {
         |        width: 320px;
         |      }
         |      @viewport {
         |        width: 320px;
         |      }
         |    }
         |  </style>
         |  <!--<![endif]-->
         |  <!--[if mso]>
         |        <xml>
         |        <o:OfficeDocumentSettings>
         |          <o:AllowPNG/>
         |          <o:PixelsPerInch>96</o:PixelsPerInch>
         |        </o:OfficeDocumentSettings>
         |        </xml>
         |        <![endif]-->
         |  <!--[if lte mso 11]>
         |        <style type="text/css">
         |          .outlook-group-fix { width:100% !important; }
         |        </style>
         |        <![endif]-->
         |
         |  <!--[if !mso]><!-->
         |  <link href="https://fonts.googleapis.com/css?family=Noto+Sans:400,700" rel="stylesheet" type="text/css">
         |  <style type="text/css">
         |    @import url(https://fonts.googleapis.com/css?family=Noto+Sans:400,700);
         |  </style>
         |  <!--<![endif]-->
         |
         |
         |
         |  <style type="text/css">
         |    @media only screen and (min-width:480px) {
         |      .mj-column-per-100 {
         |        width: 100% !important;
         |      }
         |    }
         |  </style>
         |
         |
         |  <style type="text/css">
         |  </style>
         |
         |</head>
         |
         |<body>
         |<div>
         |    <!--[if mso | IE]>
         |      <table align="center" border="0" cellpadding="0" cellspacing="0" style="width:600px;" width="600">
         |        <tr>
         |          <td style="line-height:0px;font-size:0px;mso-line-height-rule:exactly;">
         |      <![endif]-->
         |    <div style="Margin:0px auto;max-width:600px;">
         |
         |      <table align="center" border="0" cellpadding="0" cellspacing="0" role="presentation" style="width:100%;">
         |        <tbody>
         |          <tr>
         |            <td style="direction:ltr;font-size:0px;padding:20px 0;text-align:center;vertical-align:top;">
         |              <!--[if mso | IE]>
         |                  <table role="presentation" border="0" cellpadding="0" cellspacing="0">
         |
         |        <tr>
         |
         |            <td
         |               style="vertical-align:top;width:600px;"
         |            >
         |          <![endif]-->
         |
         |              <div class="mj-column-per-100 outlook-group-fix" style="font-size:13px;text-align:left;direction:ltr;display:inline-block;vertical-align:top;width:100%;">
         |
         |                <table border="0" cellpadding="0" cellspacing="0" role="presentation" style="vertical-align:top;" width="100%">
         |                  <tr>
         |                    <td style="font-size:0px;padding:10px 25px;word-break:break-word;">
         |
         |                      <p style="border-top:solid 4px #3c8e85;font-size:1;margin:0px auto;width:100%;">
         |                      </p>
         |
         |                      <!--[if mso | IE]>
         |        <table
         |           align="center" border="0" cellpadding="0" cellspacing="0" style="border-top:solid 4px #3c8e85;font-size:1;margin:0px auto;width:550px;" role="presentation" width="550px"
         |        >
         |          <tr>
         |            <td style="height:0;line-height:0;">
         |              &nbsp;
         |            </td>
         |          </tr>
         |        </table>
         |      <![endif]-->
         |
         |
         |                    </td>
         |                  </tr>
         |
         |                  <tr>
         |                    <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
         |
         |                      <div style="font-family:Noto Sans;font-size:20px;line-height:1;text-align:center;color:#3c8e85;">$subject</div>
         |                  </td>
         |               </tr>
         |
         |                  <tr>
         |                    <td style="font-size:0px;word-break:break-word;">
         |
         |
         |                      <!--[if mso | IE]>
         |
         |        <table role="presentation" border="0" cellpadding="0" cellspacing="0"><tr><td height="10" style="vertical-align:top;height:10px;">
         |
         |    <![endif]-->
         |
         |                      <div style="height:10px;">
         |                        &nbsp;
         |                      </div>
         |
         |                      <!--[if mso | IE]>
         |
         |        </td></tr></table>
         |
         |    <![endif]-->
         |
         |
         |                    </td>
         |                  </tr>
         |
         |                  $content
         |
         |                  <tr>
         |                    <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
         |
         |                      <div style="font-family:Noto Sans;font-size:14px;line-height:1;text-align:center;color:grey;">
         |                        Your Toolkit Team
         |                      </div>
         |
         |                    </td>
         |                  </tr>
         |
         |                  <tr>
         |                    <td style="font-size:0px;word-break:break-word;">
         |
         |
         |                      <!--[if mso | IE]>
         |
         |        <table role="presentation" border="0" cellpadding="0" cellspacing="0"><tr><td height="10" style="vertical-align:top;height:10px;">
         |
         |    <![endif]-->
         |
         |                      <div style="height:10px;">
         |                        &nbsp;
         |                      </div>
         |
         |                      <!--[if mso | IE]>
         |
         |        </td></tr></table>
         |
         |    <![endif]-->
         |
         |
         |                    </td>
         |                  </tr>
         |
         |                  <tr>
         |                    <td style="font-size:0px;padding:10px 25px;word-break:break-word;">
         |
         |                      <p style="border-top:solid 4px #3c8e85;font-size:1;margin:0px auto;width:100%;">
         |                      </p>
         |
         |                      <!--[if mso | IE]>
         |        <table
         |           align="center" border="0" cellpadding="0" cellspacing="0" style="border-top:solid 4px #3c8e85;font-size:1;margin:0px auto;width:550px;" role="presentation" width="550px"
         |        >
         |          <tr>
         |            <td style="height:0;line-height:0;">
         |              &nbsp;
         |            </td>
         |          </tr>
         |        </table>
         |      <![endif]-->
         |
         |
         |                    </td>
         |                  </tr>
         |
         |                </table>
         |
         |              </div>
         |
         |              <!--[if mso | IE]>
         |            </td>
         |
         |        </tr>
         |
         |                  </table>
         |                <![endif]-->
         |            </td>
         |          </tr>
         |        </tbody>
         |      </table>
         |
         |    </div>
         |
         |
         |    <!--[if mso | IE]>
         |          </td>
         |        </tr>
         |      </table>
         |      <![endif]-->
         |  </div>
         |
         |</body>
         |
         |</html>
    """.stripMargin

  def environment: play.Environment

  def env: Env

  val origin: String =
    if (environment.isProd) s"https://toolkit.tuebingen.mpg.de"
    else s"http://${env.get("HOSTNAME")}:${env.get("PORT")}"

}

object MailTemplate {

  // Date time format for the "deleting your account on" mail
  val dtf = "EEEE, dd.MM.yyyy"

  case class NewUserWelcomeMail(userParam: User, token: String, environment: play.Environment, env: Env)
      extends MailTemplate {
    override def subject = "Account Verification - The MPI Bioinformatics Toolkit"

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
      val verificationLink = s"$origin/verification/${user.getUserData.nameLogin}/$token"
      super.bodyHtmlTemplate(
        subject,
        s"""<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:14px;line-height:1;text-align:center;color:grey;">
           |      Welcome ${user.getUserData.nameLogin},<br/><br/>
           |      your registration was successful. Please take a moment and verify that this is indeed your E-Mail account.<br />
           |      </div>
           |  </td>
           |</tr>
           |
           |<tr>
           |  <td align="center" vertical-align="middle" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <table align="center" border="0" cellpadding="0" cellspacing="0" role="presentation" style="border-collapse:separate;line-height:100%;">
           |      <tr>
           |        <td align="center" bgcolor="#3c8e85" role="presentation" style="border:none;border-radius:3px;color:white;cursor:auto;padding:10px 25px;" valign="middle">
           |           <a href="$verificationLink" style="background:#3c8e85;color:white;font-family:Noto Sans;font-size:13px;font-weight:normal;line-height:120%;Margin:0;text-decoration:none;text-transform:none;" target="_blank">
           |              Verify E-Mail address
           |            </a>
           |        </td>
           |      </tr>
           |    </table>
           |  </td>
           |</tr>
           |<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:10px;line-height:1;text-align:center;color:grey;">
           |      Or copy this URL and visit the page in your browser:<br/><br/> $verificationLink
           |    </div>
           |  </td>
           |</tr>
     """.stripMargin
      )
    }
  }

  case class ChangePasswordMail(userParam: User, token: String, environment: play.Environment, env: Env)
      extends MailTemplate {
    override def subject = "Password Verification - The MPI Bioinformatics Toolkit"

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
      val verificationLink = s"$origin/verification/${user.getUserData.nameLogin}/$token"
      super.bodyHtmlTemplate(
        subject,
        s"""<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:14px;line-height:1;text-align:center;color:grey;">
           |      Dear ${user.getUserData.nameLogin},<br/><br/>
           |      you requested a password change. Please take a moment to verify and complete the process.<br/>
           |    </div>
           |  </td>
           |</tr>
           |
           |<tr>
           |  <td align="center" vertical-align="middle" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <table align="center" border="0" cellpadding="0" cellspacing="0" role="presentation" style="border-collapse:separate;line-height:100%;">
           |      <tr>
           |        <td align="center" bgcolor="#3c8e85" role="presentation" style="border:none;border-radius:3px;color:white;cursor:auto;padding:10px 25px;" valign="middle">
           |           <a href="$verificationLink" style="background:#3c8e85;color:white;font-family:Noto Sans;font-size:13px;font-weight:normal;line-height:120%;Margin:0;text-decoration:none;text-transform:none;" target="_blank">
           |              Reset Password
           |            </a>
           |        </td>
           |      </tr>
           |    </table>
           |  </td>
           |</tr>
           |<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:10px;line-height:1;text-align:center;color:grey;">
           |      Or copy this URL and visit the page in your browser:<br/><br/> $verificationLink
           |    </div>
           |  </td>
           |</tr>
           |
           |<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:14px;line-height:1;text-align:center;color:grey;">
           |      If you did not request this, then your account has been used by someone else.<br />
           |      Log in and change the password yourself to ensure that this other person can no longer access your account.<br />
           |    </div>
           |  </td>
           |</tr>
     """.stripMargin
      )
    }
  }

  case class ResetPasswordMail(userParam: User, token: String, environment: play.Environment, env: Env)
      extends MailTemplate {
    override def subject = "Password Verification - The MPI Bioinformatics Toolkit"

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
      val verificationLink = s"$origin/verification/${user.getUserData.nameLogin}/$token"
      super.bodyHtmlTemplate(
        subject,
        s"""<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:14px;line-height:1;text-align:center;color:grey;">
           |      Dear ${user.getUserData.nameLogin},<br/><br/>
           |      you requested to reset your password and set a new one. Please take a moment to complete the process.<br/>
           |    </div>
           |  </td>
           |</tr>
           |
           |<tr>
           |  <td align="center" vertical-align="middle" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <table align="center" border="0" cellpadding="0" cellspacing="0" role="presentation" style="border-collapse:separate;line-height:100%;">
           |      <tr>
           |        <td align="center" bgcolor="#3c8e85" role="presentation" style="border:none;border-radius:3px;color:white;cursor:auto;padding:10px 25px;" valign="middle">
           |           <a href="$verificationLink" style="background:#3c8e85;color:white;font-family:Noto Sans;font-size:13px;font-weight:normal;line-height:120%;Margin:0;text-decoration:none;text-transform:none;" target="_blank">
           |              Reset Password
           |            </a>
           |        </td>
           |      </tr>
           |    </table>
           |  </td>
           |</tr>
           |<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:10px;line-height:1;text-align:center;color:grey;">
           |      Or copy this URL and visit the page in your browser:<br/><br/> $verificationLink
           |    </div>
           |  </td>
           |</tr>
           |
           |<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:14px;line-height:1;text-align:center;color:grey;">
           |      If you did not request this, then your account has been used by someone else.<br />
           |      Log in and change the password yourself to ensure that this other person can no longer access your account.<br />
           |    </div>
           |  </td>
           |</tr>
     """.stripMargin
      )
    }
  }

  case class PasswordChangedMail(userParam: User, environment: play.Environment, env: Env) extends MailTemplate {
    override def subject = "Password Changed - The MPI Bioinformatics Toolkit"

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
        subject,
        s"""<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:14px;line-height:1;text-align:center;color:grey;">
           |      Dear ${user.getUserData.nameLogin},<br/><br/>
           |      your password was successfully reset.<br/>
           |      You can change it at any time in your user profile.<br/><br/>
           |      If you did not request this, then someone else may have changed your password.<br/>
           |    </div>
           |  </td>
           |</tr>
     """.stripMargin
      )
    }
  }

  case class JobFinishedMail(
      userParam: User,
      jobId: String,
      jobState: JobState,
      environment: play.Environment,
      env: Env
  ) extends MailTemplate {
    override def subject: String = s"""Job $jobId finished running - The MPI Bioinformatics Toolkit""".stripMargin

    val user: User = userParam

    def statusMessage: String = {
      jobState match {
        case Done  => "your job has finished successfully. You can now look at the results."
        case Error => "your job has failed. Please check all parameters and see if you find any issues."
        case _     => "your job has changed state."
      }
    }

    val bodyText: String = {
      s"""Dear ${user.getUserData.nameLogin},
         |$statusMessage
         |you can view it at any time at $origin/jobs/$jobId
         |Your Toolkit Team
     """.stripMargin
    }

    val bodyHtml: String = {
      val jobLink = s"$origin/jobs/$jobId"
      super.bodyHtmlTemplate(
        subject,
        s"""<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:14px;line-height:1;text-align:center;color:grey;">
           |      Dear ${user.getUserData.nameLogin},<br/><br/>
           |      $statusMessage<br/>
           |    </div>
           |  </td>
           |</tr>
           |
           |<tr>
           |  <td align="center" vertical-align="middle" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <table align="center" border="0" cellpadding="0" cellspacing="0" role="presentation" style="border-collapse:separate;line-height:100%;">
           |      <tr>
           |        <td align="center" bgcolor="#3c8e85" role="presentation" style="border:none;border-radius:3px;color:white;cursor:auto;padding:10px 25px;" valign="middle">
           |           <a href="$jobLink" style="background:#3c8e85;color:white;font-family:Noto Sans;font-size:13px;font-weight:normal;line-height:120%;Margin:0;text-decoration:none;text-transform:none;" target="_blank">
           |              View job
           |            </a>
           |        </td>
           |      </tr>
           |    </table>
           |  </td>
           |</tr>
           |<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:10px;line-height:1;text-align:center;color:grey;">
           |      Or copy this URL and visit the page in your browser:<br/><br/> $jobLink
           |    </div>
           |  </td>
           |</tr>
     """.stripMargin
      )
    }
  }

  case class OldAccountEmail(userParam: User, deletionDate: ZonedDateTime, environment: play.Environment, env: Env)
      extends MailTemplate {
    override def subject = "Old Account - The MPI Bioinformatics Toolkit"

    val user: User = userParam

    val bodyText: String = {
      s"""Dear ${user.getUserData.nameLogin},
         |we have noticed, that you have not logged in since ${user.dateLastLogin
           .map(_.format(ZonedDateTimeHelper.dateTimeFormatter))
           .getOrElse("[date not supplied]")}.
         |To keep our system running smoothly and to keep the data we collect from our users to a minimum,
         |we delete old user accounts.
         |This is why Your account will be deleted on ${user.dateLastLogin
           .map(_.format(ZonedDateTimeHelper.dateTimeFormatter))
           .getOrElse("[date not supplied]")}.
         |Just log into Your account to let us know that You are still interested in our services.
         |
       |Your Toolkit Team
         |
       |$origin
     """.stripMargin
    }

    val bodyHtml: String = {
      super.bodyHtmlTemplate(
        subject,
        s"""<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:14px;line-height:1;text-align:center;color:grey;">
           |      Dear ${user.getUserData.nameLogin},<br/><br/>
           |      we have noticed, that you have not logged in since
           |      ${user.dateLastLogin
             .map(_.format(ZonedDateTimeHelper.dateTimeFormatter))
             .getOrElse("[date not supplied]")}.<br/><br/>
           |      To keep our system running smoothly and to keep the data we collect from our users to a minimum,
           |      we remove unused user accounts.<br/><br/>
           |      This is why your account will be deleted on<br />
           |      ${user.dateLastLogin
             .map(_.format(ZonedDateTimeHelper.dateTimeFormatter))
             .getOrElse("[date not supplied]")}.<br/><br/>
           |      If you wish to continue using our services, log in before the specified date to let us know that you are still interested in our services.
           |    </div>
           |  </td>
           |</tr>
           |
           |<tr>
           |  <td align="center" vertical-align="middle" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <table align="center" border="0" cellpadding="0" cellspacing="0" role="presentation" style="border-collapse:separate;line-height:100%;">
           |      <tr>
           |        <td align="center" bgcolor="#3c8e85" role="presentation" style="border:none;border-radius:3px;color:white;cursor:auto;padding:10px 25px;" valign="middle">
           |           <a href="$origin" style="background:#3c8e85;color:white;font-family:Noto Sans;font-size:13px;font-weight:normal;line-height:120%;Margin:0;text-decoration:none;text-transform:none;" target="_blank">
           |              Log In
           |            </a>
           |        </td>
           |      </tr>
           |    </table>
           |  </td>
           |</tr>
     """.stripMargin
      )
    }
  }

}
