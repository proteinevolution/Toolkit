package controllers

import javax.inject.{Inject, Singleton}


import models.database.User
import models.sessions.Session
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Controller, Action}

/**
  * Created by zin on 28.07.16.
  */
@Singleton
final class Backend @Inject()(webJarAssets: WebJarAssets,
                              authController: Auth,
                              settingsController: Settings,
                              contentController: Content,
                              val messagesApi: MessagesApi) extends Controller with I18nSupport with Common {



  def index = Action { implicit ctx =>

    val sessionID = Session.requestSessionID(ctx)
    val user : User = Session.getUser
    if(ctx.headers.get("referer").getOrElse("").matches("http://" + ctx.host + "/@/backend.*") && !authController.loggedOut) {

      NoCache(Ok(views.html.backend.backend(webJarAssets, views.html.backend.backend_maincontent(),"Backend")).withSession {
      Session.closeSessionRequest(ctx, sessionID)
    })
    } else {

      Status(404)(views.html.errors.pagenotfound())

  }}



  def settings = Action { implicit ctx =>

    val sessionID = Session.requestSessionID(ctx)
    val user : User = Session.getUser

    if(ctx.headers.get("referer").getOrElse("").matches("http://" + ctx.host + "/@/backend.*") && !authController.loggedOut) {

      NoCache(Ok(views.html.backend.backend(webJarAssets, views.html.backend.settings(settingsController.clusterMode),"Backend")).withSession {
        Session.closeSessionRequest(ctx, sessionID)
      })

    } else {

      Status(404)(views.html.errors.pagenotfound())

    }

  }


  def edit = Action { implicit ctx =>

    val sessionID = Session.requestSessionID(ctx)
    val user : User = Session.getUser

    val content = "<h6> Recent Updates </h6>\n<hr>\n<b>February, 2016</b>\n <p class=\"message\" align=\"justify\"> PfamA 29.0 database is now also available for HHpred. </p>\n<b>January, 2016</b>\n<p class=\"message\" align=\"justify\"> BLAST+ has been updated to version 2.3.0 and MODELLER to version\n9.16. FRpred uses the BioJS Tree Viewer for displaying UPGMA dendrograms and the multiple sequence alignment\ntool GLProbs has been added to our alignment section. HHpred bugfix: probability cut-off is working again. New PatternSearch feature: found patterns are highlighted also in the export file.</p>\n<b>December, 2015</b>\n<p class=\"message\" align=\"justiy\"> ANCESCON and AlignmentViewer now use the BioJS Tree Viewer and\nthe BioJS MSA Viewer instead of the Archaeopteryx Tree Viewer applet and instead of the JalView applet,\nrespectively. Now, these tools are also fully functional in the Chrome browser. PSI-BLAST+ and ProtBLAST+ now use BLAST+ 2.2.31. </p>" // TODO get the current content from the db

    if(ctx.headers.get("referer").getOrElse("").matches("http://" + ctx.host + "/@/backend.*") && !authController.loggedOut) {

      Ok(views.html.backend.backend(webJarAssets, views.html.backend.edit(webJarAssets, content),"Backend")).withSession {
        Session.closeSessionRequest(ctx, sessionID)
      }

    } else {

      Status(404)(views.html.errors.pagenotfound())

    }

  }


  def log = {

  }




}
