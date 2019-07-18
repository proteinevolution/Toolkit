/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    resultViews: Seq[Map[String, String]],
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
