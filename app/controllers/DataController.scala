package controllers

import java.time.ZonedDateTime
import javax.inject.Inject
import play.twirl.api.Html
import models.database.CMS.FeaturedArticle
import play.api.mvc._
import reactivemongo.bson.BSONObjectID
import play.api.libs.json.{ JsArray, JsObject, Json }
import models.database.results.{ Hmmer, PSIBlast }
import modules.db.MongoStore

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by lzimmermann on 26.01.17.
  */
class DataController @Inject()(mongoStore: MongoStore,
                               psiblastController: PSIBlastController,
                               hmmerController: HmmerController,
                               hmmer: Hmmer,
                               psi: PSIBlast,
                               cc: ControllerComponents)
    extends AbstractController(cc) {

  /** Check whether the user is allowed to fetch the data for the particular job and retrieves the data with
    * stored given a particular key
    */
  def get(jobID: String): Action[AnyContent] = Action.async {
    mongoStore.getResult(jobID).map {
      case Some(jsValue) => Ok(jsValue)
      case None          => NotFound
    }
  }

  /**
    * Action to fetch article with articleID from database
    */
  def fetchArticle(articleID: String): Action[AnyContent] = Action.async {
    mongoStore.getArticle(articleID).map {
      case Some(realArticle) => Ok
      case None              => NotFound
    }
  }

  /**
    * Action to fetch the last N recent articles database
    */
  def getRecentArticles(numArticles: Int): Action[AnyContent] = Action.async {
    mongoStore.getArticles(numArticles).map { seq =>
      val x = Json.toJson(seq)
      Ok(x)
    }
  }

  /**
    * Action to write an article into the database
    */
  def writeArticle(title: String,
                   text: String,
                   textlong: String,
                   link: String,
                   imagePath: String): Action[AnyContent] = Action.async {
    // TODO ensure that only authorized people can write a front page article
    val article =
      FeaturedArticle(BSONObjectID.generate(), title, text, textlong, link, imagePath, Some(ZonedDateTime.now), None)
    mongoStore.writeArticleDatabase(article).map { wr =>
      if (wr.ok) {
        Ok
      } else {
        BadRequest
      }
    }
  }


  def getHelp(toolname: String) = Action {
    val help = toolname match{
  case "psiblast" => views.html.help.psiblast()
  case "hhblits" => views.html.help.hhblits()
  case "hhpred" => views.html.help.hhpred()
  case "hmmer" => views.html.help.hmmer()
  case "patsearch" => views.html.help.patsearch()
  case "clustalo" => views.html.help.clustalo()
  case "kalign" => views.html.help.kalign()
  case "mafft" => views.html.help.mafft()
  case "msaprobs" => views.html.help.msaprobs()
  case "muscle" => views.html.help.muscle()
  case "tcoffee" => views.html.help.tcoffee()
  case "aln2plot" => views.html.help.aln2plot()
  }
  Ok(help)
  }

}

case class DTParam(sSearch: String, iDisplayStart: Int, iDisplayLength: Int, iSortCol: Int, sSortDir: String)
