package de.proteinevolution.models.database.CMS

/**
  * Created by drau on 30.01.17.
  */
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import de.proteinevolution.models.util.ZonedDateTimeHelper
import play.api.libs.json._
import reactivemongo.bson._
import reactivemongo.play.json._

case class FeaturedArticle(
    articleID: BSONObjectID, // ID of the Article
    title: String, //Title of the Article
    text: String, // Article
    textlong: String,
    link: String,
    imagePath: String, // path of the image
    dateCreated: Option[ZonedDateTime], // Creation time of the Article
    dateUpdated: Option[ZonedDateTime]
) // Last changed on (set this when replaced by a newer version)

object FeaturedArticle {
  // Constants for the JSON object identifiers
  val ID          = "id" // name for the ID in scala
  val IDDB        = "_id" //              ID in MongoDB
  val TITLE       = "title"
  val TEXT        = "text"
  val TEXTLONG    = "textlong"
  val LINK        = "link"
  val IMAGEPATH   = "imagePath"
  val DATECREATED = "dateCreated" //              created on field
  val DATEUPDATED = "dateUpdated" //              changed on field

  implicit object JsonReader extends Reads[FeaturedArticle] {
    override def reads(json: JsValue): JsResult[FeaturedArticle] = json match {
      case obj: JsObject =>
        try {
          val mainID      = (obj \ ID).asOpt[String]
          val dateCreated = (obj \ DATECREATED).asOpt[String]
          val dateUpdated = (obj \ DATEUPDATED).asOpt[String]
          JsSuccess(
            FeaturedArticle(
              articleID = BSONObjectID.generate(),
              title = "",
              text = "",
              textlong = "",
              link = "",
              imagePath = "",
              dateCreated = Some(ZonedDateTime.now),
              dateUpdated = Some(ZonedDateTime.now)
            )
          )
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object ArticleWrites extends Writes[FeaturedArticle] {
    def writes(featuredArticle: FeaturedArticle): JsObject = Json.obj(
      IDDB        -> featuredArticle.articleID,
      TITLE       -> featuredArticle.title,
      TEXT        -> featuredArticle.text,
      TEXTLONG    -> featuredArticle.textlong,
      LINK        -> featuredArticle.link,
      IMAGEPATH   -> featuredArticle.imagePath,
      DATECREATED -> featuredArticle.dateCreated.map(_.format(ZonedDateTimeHelper.dateTimeFormatter)),
      DATEUPDATED -> featuredArticle.dateUpdated.map(_.format(ZonedDateTimeHelper.dateTimeFormatter))
    )
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[FeaturedArticle] {
    def read(bson: BSONDocument): FeaturedArticle = {
      FeaturedArticle(
        articleID = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        title = bson.getAs[String](TITLE).get,
        text = bson.getAs[String](TEXT).getOrElse("Error Loading Article."),
        textlong = bson.getAs[String](TEXTLONG).getOrElse("Error Loading Article."),
        link = bson.getAs[String](LINK).get,
        imagePath = bson.getAs[String](IMAGEPATH).getOrElse("Error Loading URL."),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => ZonedDateTimeHelper.getZDT(dt))
      )
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[FeaturedArticle] {
    def write(featuredArticle: FeaturedArticle): BSONDocument = BSONDocument(
      IDDB        -> featuredArticle.articleID,
      TITLE       -> featuredArticle.title,
      TEXT        -> featuredArticle.text,
      TEXTLONG    -> featuredArticle.textlong,
      LINK        -> featuredArticle.link,
      IMAGEPATH   -> featuredArticle.imagePath,
      DATECREATED -> BSONDateTime(featuredArticle.dateCreated.fold(-1L)(_.toInstant.toEpochMilli)),
      DATEUPDATED -> BSONDateTime(featuredArticle.dateUpdated.fold(-1L)(_.toInstant.toEpochMilli))
    )
  }

}
