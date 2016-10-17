package controllers

import javax.inject.{Inject, Singleton}

import models.Values
import models.tools.ToolModel2
import models.tools.ToolModel2.Toolitem
import play.api.Logger
import play.api.cache.{CacheApi, _}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import play.api.mvc.{Action, Controller}
import play.api.libs.json.JsArray
import play.api.libs.json.JsError
import play.api.libs.functional.syntax._


/**
  *  Just for some Testing purposes
  *
  * Created by lzimmermann on 10/14/16.
  */
@Singleton
class TestController @Inject() (val values: Values,
                                @NamedCache("toolitemCache") val toolitemCache: CacheApi) extends Controller {


  // TODO Add validation
  // TODO Condense to Format
  implicit val toolReads: Reads[Toolitem] = (
      (JsPath \ "toolname").read[String] and
      (JsPath \ "toolnameLong").read[String] and
      (JsPath \ "toolnameAbbrev").read[String] and
      (JsPath \ "category").read[String] and
      (JsPath \ "params").read[Seq[(String, Seq[(String, Seq[(String, String)])])]]
    )(Toolitem.apply _)

  implicit val toolWrites: Writes[Toolitem] = (
      (JsPath \ "toolname").write[String] and
      (JsPath \ "toolnameLong").write[String] and
      (JsPath \ "toolnameAbbrev").write[String] and
      (JsPath \ "category").write[String] and
      (JsPath \ "params").write[Seq[(String, Seq[(String, Seq[(String, String)])])]]
    )(unlift(Toolitem.unapply))


  implicit def tuple2Reads[A, B](implicit aReads: Reads[A], bReads: Reads[B]): Reads[(A, B)] = Reads[(A, B)] {
    case JsArray(arr) if arr.size == 2 => for {
      a <- aReads.reads(arr.head)
      b <- bReads.reads(arr(1))
    } yield (a, b)
    case _ => JsError(Seq(JsPath() -> Seq(ValidationError("Expected array of three elements"))))
  }

  implicit def tuple2Writes[A, B](implicit a: Writes[A], b: Writes[B]): Writes[(A,B)] = new Writes[(A,B)] {
    def writes(tuple: (A,B)) = JsArray(Seq(a.writes(tuple._1), b.writes(tuple._2)))
  }

  def getTool(toolname : String) = Action {

    Ok(Json.toJson(toolitemCache.getOrElse(toolname) {
      val x = ToolModel2.toolMap(toolname).toolitem(values)   // Reset toolitem in cache
      toolitemCache.set(toolname, x)
      x
    }))
  }
}
