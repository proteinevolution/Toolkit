package controllers

import javax.inject.Inject

import models.database.CMS.FeaturedArticle
import models.datatables.HitListDAL.PSIBlastDTParam
import models.datatables.HitlistDAL
import modules.CommonModule
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json}
import play.api.Logger

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by lzimmermann on 26.01.17.
  */
class DataController  @Inject() (val reactiveMongoApi: ReactiveMongoApi, sup: HitlistDAL)
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

  def psiDT(jobID : String) : Action[AnyContent] = Action { implicit request =>

    val params = PSIBlastDTParam(
      request.getQueryString("sSearch").getOrElse(""),
      request.getQueryString("iDisplayStart").getOrElse("0").toInt,
      request.getQueryString("iDisplayLength").getOrElse("10").toInt,
      request.getQueryString("iSortCol_0").getOrElse("1").toInt,
      request.getQueryString("sSortDir_0").getOrElse("asc"))


    val totalHits = sup.getHits(jobID).map {
      x => x.length
    }

    val hits = sup.getHitsByKeyWord(jobID, params).map {
      x => x
    }

    Logger.info(":::::::::" + params)

    val hitsOrderBy = (params.iSortCol, params.sSortDir) match {
      case (1, "asc") => hits.map(x => x.sortBy(_.accession))
      case (1, "desc") => hits.map(x => x.sortWith(_.accession > _.accession))
      case (2, "asc") => hits.map(x => x.sortBy(_.description))
      case (2, "desc") => hits.map(x => x.sortWith(_.description > _.description))
      case (3, "asc") => hits.map(x => x.sortBy(_.evalue))
      case (3, "desc") => hits.map(x => x.sortWith(_.evalue > _.evalue))
      case (4, "asc") => hits.map(x => x.sortBy(_.score))
      case (4, "desc") => hits.map(x => x.sortWith(_.score > _.score))
      case (5, "asc") => hits.map(x => x.sortBy(_.bitscore))
      case (5, "desc") => hits.map(x => x.sortWith(_.bitscore > _.bitscore))
      case (6, "asc") => hits.map(x => x.sortBy(_.identity))
      case (6, "desc") => hits.map(x => x.sortWith(_.identity > _.identity))
      case (7, "asc") => hits.map(x => x.sortBy(_.hit_len))
      case (7, "desc") => hits.map(x => x.sortWith(_.hit_len > _.hit_len))
      case (_, _) => hits.map(x => x.sortBy(_.num))
    }


    //println("TEST123" + Await.result(hitsOrderBy, scala.concurrent.duration.Duration(1, "seconds")).head.hit_len)


    val jsonObject = Json.toJson(Map("aaData" -> Await.result(hitsOrderBy, scala.concurrent.duration.Duration(1, "seconds")).map(_.toDataTable)))

    val dataTableJson = Json.toJson(Map("iTotalRecords" -> Await.result(totalHits, scala.concurrent.duration.Duration(1, "seconds")),
      "iTotalDisplayRecords" -> Await.result(totalHits, scala.concurrent.duration.Duration(1, "seconds")))).as[JsObject].deepMerge(jsonObject.as[JsObject])


    Ok(dataTableJson)

  }



}





