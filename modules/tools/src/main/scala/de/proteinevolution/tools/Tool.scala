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

import de.proteinevolution.params.Param
import de.proteinevolution.tools.forms.ToolForm

case class Tool(
    toolNameShort: String,
    toolNameLong: String,
    code: String,
    category: String,
    params: Map[String, Param],
    toolForm: ToolForm,
    paramGroups: Map[String, Seq[String]],
    forwardAlignment: Seq[String],
    forwardMultiSeq: Seq[String],
    title: String
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
