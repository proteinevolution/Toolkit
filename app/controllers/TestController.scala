package controllers

import javax.inject.{Inject, Singleton}

import models.Values
import models.tools.ToolModel2
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
class TestController @Inject() (val values: Values) extends Controller {

  case class Tool(toolname : String,
                  toolnameLong : String,
                  toolnameAbbrev : String,
                  category : String,
                  params : Seq[(String, Seq[(String, Seq[(String, String)])])])

  // TODO Add validation
  // TODO Condense to Format
  implicit val toolReads: Reads[Tool] = (
      (JsPath \ "toolname").read[String] and
      (JsPath \ "toolnameLong").read[String] and
      (JsPath \ "toolnameAbbrev").read[String] and
      (JsPath \ "category").read[String] and
      (JsPath \ "params").read[Seq[(String, Seq[(String, Seq[(String, String)])])]]
    )(Tool.apply _)

  implicit val toolWrites: Writes[Tool] = (
      (JsPath \ "toolname").write[String] and
      (JsPath \ "toolnameLong").write[String] and
      (JsPath \ "toolnameAbbrev").write[String] and
      (JsPath \ "category").write[String] and
      (JsPath \ "params").write[Seq[(String, Seq[(String, Seq[(String, String)])])]]
    )(unlift(Tool.unapply))


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

     val toolModel = ToolModel2.toolMap(toolname)

    // assemble parameter representation
     val params : Seq[(String, Seq[(String, Seq[(String, String)])])] = toolModel.paramGroups.keysIterator.map { group =>

       group ->  toolModel.paramGroups(group).filter(toolModel.params.contains(_)).map { param =>

         param -> values.allowed.getOrElse(param, Seq.empty)
       }
     }.toSeq

    val remainParams = toolModel.remainParamName -> toolModel.remainParams.map { param =>

      param -> values.allowed.getOrElse(param, Seq.empty)

    }

    Ok(Json.toJson(Tool(toolModel.toolNameShort,
                        toolModel.toolNameLong,
                        toolModel.toolNameAbbrev,
                        toolModel.category, params :+ remainParams
      )))
  }
}

/*



 toolModel.paramGroups
        .mapValues { vals =>
          views.html.jobs.parampanel(values, vals.filter(toolModel.params.contains(_)), ToolModel2.jobForm)
        } + (toolModel.remainParamName -> views.html.jobs.parampanel(values, toolModel.remainParams, ToolModel2.jobForm))
      viewCache.set(toolname, x)

 */

