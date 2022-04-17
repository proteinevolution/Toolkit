/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.auth.models

import de.proteinevolution.common.models.database.jobs.JobState.{ Done, Error }
import de.proteinevolution.common.models.database.jobs._
import de.proteinevolution.common.models.util.ZonedDateTimeHelper
import play.api.Configuration
import de.proteinevolution.user.User
import play.api.libs.mailer.{ Email, MailerClient }

sealed trait MailTemplate {

  val user: User
  def subject = "Generic Information"
  val bodyText: String
  val bodyHtml: String

  def userName(): String = user.userData.get.nameLogin

  def send(implicit mailerClient: MailerClient): Unit = {
    if (!user.isRegistered) {
      throw new IllegalArgumentException("Sending an email is only possible if the user is registered")
    }
    val email = Email(
      subject,
      "Toolkit Team <mpi-toolkit@tuebingen.mpg.de>",
      Seq(userName() + " <" + user.userData.get.eMail + ">"),
      attachments = Seq(),
      bodyText = Some(this.bodyText), // Text version of the E-Mail in case the User has no HTML E-Mail client
      bodyHtml = Some(this.bodyHtml)  // HTML formatted E-Mail content
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

  def config: Configuration

  val origin: String = {
    val s = config.get[String]("mail.host")
    if (s.endsWith("/")) s.substring(0, s.length - 1) else s
  }

}

object MailTemplate {

  // Date time format for the "deleting your account on" mail
  val dtf = "EEEE, dd.MM.yyyy"

  case class NewUserWelcomeMail(userParam: User, token: String, config: Configuration) extends MailTemplate {
    override def subject = "Account Verification - The MPI Bioinformatics Toolkit"

    val user: User = userParam

    val verificationLink = s"$origin/verify/${userName()}/$token"

    val bodyText: String = {
      s"""Welcome ${userName()},
         |your registration was successful. Please take a moment and verify that this is indeed your E-Mail account.
         |To do this, visit
         |$verificationLink
         |Your Toolkit Team
     """.stripMargin
    }

    val bodyHtml: String = {
      super.bodyHtmlTemplate(
        subject,
        s"""<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:14px;line-height:1;text-align:center;color:grey;">
           |      Welcome ${userName()},<br/><br/>
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

  case class ResetPasswordMail(userParam: User, token: String, config: Configuration) extends MailTemplate {
    override def subject = "Password Verification - The MPI Bioinformatics Toolkit"

    val user: User = userParam

    val resetPasswordLink = s"$origin/reset-password/${userName()}/$token"

    val bodyText: String = {
      s"""Dear ${userName()},
         |you requested to reset your password and set a new one.
         |To complete the process, visit
         |$resetPasswordLink
         |If you did not request this, then someone may have tried to log into your account.
         |Your Toolkit Team
     """.stripMargin
    }

    val bodyHtml: String = {
      super.bodyHtmlTemplate(
        subject,
        s"""<tr>
           |  <td align="center" style="font-size:0px;padding:10px 25px;word-break:break-word;">
           |    <div style="font-family:Noto Sans;font-size:14px;line-height:1;text-align:center;color:grey;">
           |      Dear ${userName()},<br/><br/>
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
           |           <a href="$resetPasswordLink" style="background:#3c8e85;color:white;font-family:Noto Sans;font-size:13px;font-weight:normal;line-height:120%;Margin:0;text-decoration:none;text-transform:none;" target="_blank">
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
           |      Or copy this URL and visit the page in your browser:<br/><br/> $resetPasswordLink
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

  case class PasswordChangedMail(userParam: User, config: Configuration) extends MailTemplate {
    override def subject = "Password Changed - The MPI Bioinformatics Toolkit"

    val user: User = userParam

    val bodyText: String = {
      s"""Dear ${userName()},
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
           |      Dear ${userName()},<br/><br/>
           |      your password was reset successfully.<br/>
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
      config: Configuration
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
      s"""Dear ${userName()},
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
           |      Dear ${userName()},<br/><br/>
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

  case class OldAccountEmail(userParam: User, daysUntilDeletion: Int, config: Configuration) extends MailTemplate {
    override def subject = "Old Account - The MPI Bioinformatics Toolkit"

    val user: User = userParam

    val bodyText: String = {
      s"""Dear ${userName()},
         |we have noticed, that you have not logged in since ${user.dateLastLogin.format(
          ZonedDateTimeHelper.dateTimeFormatter
        )}.
         |To keep our system running smoothly and to keep the data we collect from our users to a minimum,
         |we delete old user accounts.
         |This is why your account will be deleted in $daysUntilDeletion days.
         |If you wish to continue using our services, log in before the specified date to let us know that you are still interested in our services.
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
           |      Dear ${userName()},<br/><br/>
           |      we have noticed, that you have not logged in since
           |      ${user.dateLastLogin.format(ZonedDateTimeHelper.dateTimeFormatter)}.<br/><br/>
           |      To keep our system running smoothly and to keep the data we collect from our users to a minimum,
           |      we remove unused user accounts.<br/><br/>
           |      This is why your account will be deleted in <b>$daysUntilDeletion</b> days.<br/><br/>
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
