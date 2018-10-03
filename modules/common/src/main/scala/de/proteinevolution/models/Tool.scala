package de.proteinevolution.models

import de.proteinevolution.models.forms.{ ToolForm, ToolFormSimple }
import de.proteinevolution.models.param.Param

case class Tool(
    toolNameShort: String,
    toolNameLong: String,
    order: Int,
    title: String,
    code: String,
    section: String,
    params: Map[String, Param], // Maps a parameter name to the respective Param instance
    toolForm: ToolForm,
    toolFormSimple: ToolFormSimple,
    paramGroups: Map[String, Seq[String]],
    forwardAlignment: Seq[String],
    forwardMultiSeq: Seq[String]
) {

  def isToolName(toolName: String, caseSensitive: Boolean = false): Boolean = {
    if (caseSensitive) {
      code.contains(toolName) || toolNameShort.contains(toolName) || toolNameLong.contains(toolName)
    } else {
      code.toLowerCase.contains(toolName.toLowerCase) ||
      toolNameShort.toLowerCase.contains(toolName.toLowerCase) ||
      toolNameLong.toLowerCase.contains(toolName.toLowerCase)
    }
  }

}
