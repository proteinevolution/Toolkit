package controllers

import java.time.ZonedDateTime
import javax.inject.Inject

import models.database.CMS.FeaturedArticle
import de.proteinevolution.models.database.results.{Hmmer, PSIBlast}
import modules.db.{MongoStore, ResultFileAccessor}
import play.api.libs.json.Json
import play.api.mvc._
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by lzimmermann on 26.01.17.
  */
class DataController @Inject()(mongoStore: MongoStore,
                               psiblastController: PSIBlastController,
                               hmmerController: HmmerController,
                               hmmer: Hmmer,
                               psi: PSIBlast,
                               cc: ControllerComponents,
                               resultFiles : ResultFileAccessor)
    extends AbstractController(cc) {

  /** Check whether the user is allowed to fetch the data for the particular job and retrieves the data with
    * stored given a particular key
    */
  def get(jobID: String): Action[AnyContent] = Action.async {
    resultFiles.getResults(jobID).map {
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
  case "hhrepid" => views.html.help.hhrepid()
  case "marcoil" => views.html.help.marcoil()
  case "pcoils" => views.html.help.pcoils()
  case "repper" => views.html.help.repper()
  case "tprpred" => views.html.help.tprpred()
  case "ali2d" => views.html.help.ali2d()
  case "quick2d" => views.html.help.quick2d()
  case "modeller" => views.html.help.modeller()
  case "samcc" => views.html.help.samcc()
  case "ancescon" => views.html.help.ancescon()
  case "clans" => views.html.help.clans()
  case "mmseqs2" => views.html.help.mmseqs2()
  case "phyml" => views.html.help.phyml()
  case "sixframe" => views.html.help.sixframe()
  case "backtrans" => views.html.help.backtrans()
  case "hhfilter" => views.html.help.hhfilter()
  case "retseq" => views.html.help.retseq()
  case "seq2id" => views.html.help.seq2id()
  }
  Ok(help)
  }

}
