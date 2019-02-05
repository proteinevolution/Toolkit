package de.proteinevolution.tools

import de.proteinevolution.tools.forms.ToolFormSimple
import de.proteinevolution.parameters.ToolParameters

case class Tool(
    toolNameShort: String,
    toolNameLong: String,
    order: Int,
    description: String,
    code: String,
    section: String,
    toolParameterForm: ToolParameters,
    toolFormSimple: ToolFormSimple
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
