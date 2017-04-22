package controllers

import javax.inject.Inject

import models.database.CMS.FeaturedArticle
import modules.CommonModule
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.{JsArray, JsObject, Json}

import scala.concurrent.Future
import controllers.PSIBlastController
import controllers.HmmerController
import models.database.results.{Hmmer, PSIBlast}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by lzimmermann on 26.01.17.
  */
class DataController  @Inject() (val reactiveMongoApi: ReactiveMongoApi, psiblastController: PSIBlastController, hmmerController: HmmerController, hmmer: Hmmer, psi: PSIBlast)
  extends Controller with CommonModule {

  /** Check whether the user is allowed to fetch the data for the particular job and retrieves the data with
    * stored given a particular key
   */
  def get(jobID: String) : Action[AnyContent] = Action.async {
    getResult(jobID).map {
      case Some(jsValue) => Ok(jsValue)
      case None => NotFound
    }
  }

  /**
    * Action to fetch article with articleID from database
    */

  def fetchArticle(articleID: String) : Action[AnyContent] = Action.async{
    getArticle(articleID).map {
        case Some(realArticle) => Ok
        case None => NotFound
      }
    }
  /**
    * Action to fetch the last N recent articles database
    */
  def getRecentArticles(numArticles: Int) : Action[AnyContent] = Action.async{
    getArticles(numArticles).map { seq =>
      val  x = Json.toJson(seq)
      Ok(x)
    }
  }
  /**
    * Action to write an article into the database
    */
  def writeArticle(title: String, text: String, link: String, imagePath: String) : Action[AnyContent] = Action.async{
    val article = FeaturedArticle(BSONObjectID.generate(),title, text, link,imagePath,Some(DateTime.now()),None)
    writeArticleDatabase(article).map { wr =>
      if(wr.ok){
        Ok
      } else{
        BadRequest
      }
    }
  }

  /**
    * DataTables for job results
    */

  def dataTableHmmer(jobID : String) : Action[AnyContent] = Action.async { implicit request =>
    val params = DTParam(
      request.getQueryString("sSearch").getOrElse(""),
      request.getQueryString("iDisplayStart").getOrElse("0").toInt,
      request.getQueryString("iDisplayLength").getOrElse("100").toInt,
      request.getQueryString("iSortCol_0").getOrElse("1").toInt,
      request.getQueryString("sSortDir_0").getOrElse("asc"))

    val hits = hmmerController.getHitsByKeyWord(jobID, params)
    var db = ""
    val total = getResult(jobID).map {
      case Some(jsValue) => {
        val result = hmmer.parseResult(jsValue)
        db = result.db
        result.num_hits
      }
    }
    hmmer.hitsOrderBy(params, hits).flatMap { list =>
      total.map { total_ =>
        Ok(Json.toJson(Map("iTotalRecords" -> total_, "iTotalDisplayRecords" -> total_))
          .as[JsObject].deepMerge(Json.obj("aaData" -> list.map(_.toDataTable(db)))))
      }
    }
  }

  def dataTablePSIBlast(jobID : String) : Action[AnyContent] = Action.async { implicit request =>
    val params = DTParam(
      request.getQueryString("sSearch").getOrElse(""),
      request.getQueryString("iDisplayStart").getOrElse("0").toInt,
      request.getQueryString("iDisplayLength").getOrElse("100").toInt,
      request.getQueryString("iSortCol_0").getOrElse("1").toInt,
      request.getQueryString("sSortDir_0").getOrElse("asc"))

    var db = ""
    val total = getResult(jobID).map {
      case Some(jsValue) => {
        val result = psi.parseResult(jsValue)
        db = result.db
        result.num_hits
      }
    }
    val hits = psiblastController.getHitsByKeyWord(jobID, params)

    psi.hitsOrderBy(params, hits).flatMap { list =>
      total.map { total_ =>
        Ok(Json.toJson(Map("iTotalRecords" -> total_, "iTotalDisplayRecords" -> total_))
          .as[JsObject].deepMerge(Json.obj("aaData" -> list.map(_.toDataTable(db)))))
      }
    }
  }

}

case class DTParam(sSearch: String, iDisplayStart: Int, iDisplayLength: Int, iSortCol: Int, sSortDir: String)



