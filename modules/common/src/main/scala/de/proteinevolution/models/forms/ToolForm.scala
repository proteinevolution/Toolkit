package de.proteinevolution.models.forms

import de.proteinevolution.models.param.Param
import play.api.libs.json._

// Returned to the View if a tool is requested with the getTool route
case class ToolForm(
    toolname: String,
    toolnameLong: String,
    toolnameAbbrev: String,
    category: String,
    params: Seq[(String, Seq[Param])]
)

object ToolForm {

  implicit val toolFormWrites: OWrites[ToolForm] = Json.writes[ToolForm]

}
