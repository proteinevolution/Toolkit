package models.database

import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson._
import reactivemongo.play.json._

case class Comment(commentID   : BSONObjectID,     // ID of the Comment
                   title       : Option[String]       = None, //Title of the comment
                   text        : String,           // Comment
                   commentList : List[BSONObjectID]   = List.empty, // List of child comments
                   deleted     : Option[Boolean]      = None, // Unset: Not hidden, False: Hidden by Owner, True: Deleted by Moderators
                   oldVersion  : Option[BSONObjectID] = None, // Older version of the comment (set this to link to the original comment after a edit)
                   dateCreated : Option[DateTime], // Creation time of the Comment
                   dateUpdated : Option[DateTime]) // Last changed on (set this when replaced by a newer version)

object Comment {
  // Constants for the JSON object identifiers
  val ID          = "id"            // name for the ID in scala
  val IDDB        = "_id"           //              ID in MongoDB
  val TITLE       = "title"
  val TEXT        = "text"
  val COMMENTLIST = "commentList"   //              comment list references
  val DELETED     = "deleted"
  val OLDVERSION  = "oldVersion"
  val DATECREATED = "dateCreated"   //              created on field
  val DATEUPDATED = "dateUpdated"   //              changed on field

  implicit object JsonReader extends Reads[Comment] {
    // TODO this is unused at the moment, as there is no convertion of JSON -> comment needed.
    override def reads(json: JsValue): JsResult[Comment] = json match {
      case obj: JsObject => try {
        val mainID      = (obj \ ID).asOpt[String]
        val commentList = (obj \ COMMENTLIST).asOpt[List[String]]
        val dateCreated = (obj \ DATECREATED).asOpt[String]
        val dateUpdated = (obj \ DATEUPDATED).asOpt[String]
        JsSuccess(Comment(
          commentID   = BSONObjectID.generate(),
          text = "",
          dateCreated = Some(new DateTime()),
          dateUpdated = Some(new DateTime())))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }


  implicit object CommentWrites extends Writes[Comment] {
    def writes (comment : Comment) : JsObject = Json.obj(
      IDDB        -> comment.commentID,
      TITLE       -> comment.title,
      TEXT        -> comment.text,
      COMMENTLIST -> comment.commentList,
      DELETED     -> comment.deleted,
      OLDVERSION  -> comment.oldVersion,
      DATECREATED -> comment.dateCreated,
      DATEUPDATED -> comment.dateUpdated
    )
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[Comment] {
    def read(bson : BSONDocument): Comment = {
      Comment(
        commentID   = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        title       = bson.getAs[String](TITLE),
        text        = bson.getAs[String](TEXT).getOrElse("Error Loading Comment."),
        commentList = bson.getAs[List[BSONObjectID]](COMMENTLIST).getOrElse(List.empty),
        deleted     = bson.getAs[Boolean](DELETED),
        oldVersion  = bson.getAs[BSONObjectID](OLDVERSION),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value))
      )
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[Comment] {
    def write(comment: Comment) : BSONDocument = BSONDocument(
      IDDB        -> comment.commentID,
      TITLE       -> comment.title,
      TEXT        -> comment.text,
      COMMENTLIST -> comment.commentList,
      DELETED     -> comment.deleted,
      OLDVERSION  -> comment.oldVersion,
      DATECREATED -> BSONDateTime(comment.dateCreated.fold(-1L)(_.getMillis)),
      DATEUPDATED -> BSONDateTime(comment.dateUpdated.fold(-1L)(_.getMillis))
    )
  }
}


