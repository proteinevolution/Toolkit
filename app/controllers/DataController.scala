package controllers

import javax.inject.Inject

import models.database.CMS.FeaturedArticle
import modules.CommonModule
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONObjectID
import org.joda.time.DateTime
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by lzimmermann on 26.01.17.
  */
class DataController  @Inject() (val reactiveMongoApi: ReactiveMongoApi)
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

  def psiDT(jobID : String, numiter: Int) : Action[AnyContent] = Action { implicit request =>

    Ok

  }



}





