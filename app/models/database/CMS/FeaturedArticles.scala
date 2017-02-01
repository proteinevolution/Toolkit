package models.database.CMS

/**
  * Created by drau on 30.01.17.
  */

import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson._
import reactivemongo.play.json._
import reactivemongo.bson.Macros

case class FeaturedArticle(articleID   : BSONObjectID,     // ID of the Article
                   title       : String,           //Title of the Article
                   text        : String,           // Article
                   imagePath   : String,           // path of the image
                   dateCreated : Option[DateTime], // Creation time of the Article
                   dateUpdated : Option[DateTime]) // Last changed on (set this when replaced by a newer version)

object FeaturedArticle{
  // Constants for the JSON object identifiers
  val ID          = "id"            // name for the ID in scala
  val IDDB        = "_id"           //              ID in MongoDB
  val TITLE       = "title"
  val TEXT        = "text"
  val IMAGEPATH   = "imagePath"
  val DATECREATED = "dateCreated"   //              created on field
  val DATEUPDATED = "dateUpdated"   //              changed on field

  implicit object JsonReader extends Reads[FeaturedArticle] {
    override def reads(json: JsValue): JsResult[FeaturedArticle] = json match {
      case obj: JsObject => try {
        val mainID      = (obj \ ID).asOpt[String]
        val dateCreated = (obj \ DATECREATED).asOpt[String]
        val dateUpdated = (obj \ DATEUPDATED).asOpt[String]
        JsSuccess(FeaturedArticle(
          articleID   = BSONObjectID.generate(),
          title = "",
          text = "",
          imagePath = "",
          dateCreated = Some(new DateTime()),
          dateUpdated = Some(new DateTime())))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }


  implicit object ArticleWrites extends Writes[FeaturedArticle] {
    def writes (featuredArticle : FeaturedArticle) : JsObject = Json.obj(
      IDDB        -> featuredArticle.articleID,
      TITLE       -> featuredArticle.title,
      TEXT        -> featuredArticle.text,
      DATECREATED -> featuredArticle.dateCreated,
      DATEUPDATED -> featuredArticle.dateUpdated
    )
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[FeaturedArticle] {
    def read(bson : BSONDocument): FeaturedArticle = {
      FeaturedArticle(
        articleID   = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        title       = bson.getAs[String](TITLE).get,
        text        = bson.getAs[String](TEXT).getOrElse("Error Loading Article."),
        imagePath   = bson.getAs[String](IMAGEPATH).getOrElse("Error Loading URL."),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value))
      )
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[FeaturedArticle] {
    def write(featuredArticle: FeaturedArticle) : BSONDocument = BSONDocument(
      IDDB        -> featuredArticle.articleID,
      TITLE       -> featuredArticle.title,
      TEXT        -> featuredArticle.text,
      IMAGEPATH   -> featuredArticle.imagePath,
      DATECREATED -> BSONDateTime(featuredArticle.dateCreated.fold(-1L)(_.getMillis)),
      DATEUPDATED -> BSONDateTime(featuredArticle.dateUpdated.fold(-1L)(_.getMillis))
    )
  }

}


