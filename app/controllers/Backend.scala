package controllers

import javax.inject.{Inject, Singleton}

import org.joda.time.DateTime
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by zin on 28.07.16.
  */
@Singleton
final class Backend @Inject()(webJarAssets       : WebJarAssets,
                              settingsController : Settings,
                              contentController  : Content,
     @NamedCache("userCache") userCache          : CacheApi,
                          val reactiveMongoApi   : ReactiveMongoApi,
                          val messagesApi        : MessagesApi)
                      extends Controller with I18nSupport
                                         with Common
                                         with UserSessions {


  // Maps Session ID to Actor Ref of corresponding WebSocket
  val connectedUsers = new scala.collection.mutable.HashMap[BSONObjectID, DateTime]

  def index = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>
      if (user.isSuperuser) {
        //CheckBackendPath && user.isSuperuser && connectedUsers.get(user.userID).get.isBeforeNow) {
        NoCache(Ok(views.html.backend.backend(webJarAssets, views.html.backend.backend_maincontent(), "Backend")))
      } else {
        Status(404)(views.html.errors.pagenotfound())
      }
    }
  }

  /**
    * Redirect the user to the
    *
    * @return
    */
  def access = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>
      if (user.isSuperuser) {
        connectedUsers.put(user.userID, DateTime.now().plusMinutes(10))
        NoCache(Redirect(routes.Backend.index))
      }
      else {
        Status(404)(views.html.errors.pagenotfound())
      }
    }
  }

  def logOut = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>
      val timeOpt = connectedUsers.remove(user.userID)
      NoCache(Redirect(routes.Application.index))
    }
  }


  def settings = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>
      if (CheckBackendPath) {
        NoCache(Ok(views.html.backend.backend(webJarAssets, views.html.backend.settings(settingsController.clusterMode), "Backend")))
      } else {
        Status(404)(views.html.errors.pagenotfound())
      }
    }
  }


  def edit = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>
      val content = "<h6> Recent Updates </h6>\n<hr>\n<b>February, 2016</b>\n <p class=\"message\" align=\"justify\"> PfamA 29.0 database is now also available for HHpred. </p>\n<b>January, 2016</b>\n<p class=\"message\" align=\"justify\"> BLAST+ has been updated to version 2.3.0 and MODELLER to version\n9.16. FRpred uses the BioJS Tree Viewer for displaying UPGMA dendrograms and the multiple sequence alignment\ntool GLProbs has been added to our alignment section. HHpred bugfix: probability cut-off is working again. New PatternSearch feature: found patterns are highlighted also in the export file.</p>\n<b>December, 2015</b>\n<p class=\"message\" align=\"justiy\"> ANCESCON and AlignmentViewer now use the BioJS Tree Viewer and\nthe BioJS MSA Viewer instead of the Archaeopteryx Tree Viewer applet and instead of the JalView applet,\nrespectively. Now, these tools are also fully functional in the Chrome browser. PSI-BLAST+ and ProtBLAST+ now use BLAST+ 2.2.31. </p>" // TODO get the current content from the db
      if (CheckBackendPath) {
        Ok(views.html.backend.backend(webJarAssets, views.html.backend.edit(webJarAssets, content), "Backend"))
      } else {
        Status(404)(views.html.errors.pagenotfound())
      }
    }
  }

  def log() = {

  }
}
