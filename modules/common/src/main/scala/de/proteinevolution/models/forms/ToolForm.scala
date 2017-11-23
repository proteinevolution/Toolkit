package de.proteinevolution.models.forms

import de.proteinevolution.models.param.Param

// Returned to the View if a tool is requested with the getTool route
case class ToolForm(toolname: String,
                    toolnameLong: String,
                    toolnameAbbrev: String,
                    category: String,
                    optional: String,
                    params: Seq[(String, Seq[Param])])
