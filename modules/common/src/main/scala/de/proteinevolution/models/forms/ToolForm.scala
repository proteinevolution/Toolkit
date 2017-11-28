package de.proteinevolution.models.forms

import de.proteinevolution.models.param.Param
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{ JsPath, Writes }
import play.api.libs.functional.syntax._

// Returned to the View if a tool is requested with the getTool route
case class ToolForm(toolname: String,
                    toolnameLong: String,
                    toolnameAbbrev: String,
                    category: String,
                    params: Seq[(String, Seq[Param])])

object ToolForm {

  implicit val toolFormWrites: Writes[ToolForm] = (
    (JsPath \ "toolname").write[String] and
    (JsPath \ "toolnameLong").write[String] and
    (JsPath \ "toolnameAbbrev").write[String] and
    (JsPath \ "category").write[String] and
    (JsPath \ "params").write[Seq[(String, Seq[Param])]]
  )(unlift(ToolForm.unapply))

}
