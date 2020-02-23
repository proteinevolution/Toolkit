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

import com.typesafe.config.{Config, ConfigObject}
import de.proteinevolution.tools.forms.ValidationParamsForm.{AccessionIDValidationParamsForm, EmptyValidationParamsForm, PlainTextValidationParamsForm, RegexValidationParamsForm, SequenceValidationParamsForm}
import de.proteinevolution.tools.forms.{ToolFormSimple, ValidationParamsForm}
import de.proteinevolution.tools.parameters.TextAreaInputType.TextAreaInputType
import de.proteinevolution.tools.parameters.{ForwardingMode, ParamAccess, Parameter, ParameterSection, TextAreaInputType, ToolParameters}
import javax.inject.{Inject, Singleton}
import play.api.Configuration

import scala.collection.JavaConverters._
import scala.util.Try

@Singleton
class ToolConfig @Inject()(config: Configuration, paramAccess: ParamAccess) {

  lazy val version: String = config.get[String]("version")

  lazy val values: Map[String, Tool] = {
    config.get[Config]("Tools").root.asScala.map {
      case (_, configObject: ConfigObject) =>
        val config = configObject.toConfig
        val inputType = Try(config.getString("input_type")).getOrElse(TextAreaInputType.SEQUENCE)
        config.getString("name") -> toTool(
          config.getString("name"),
          config.getString("longname"),
          config.getInt("order"),
          config.getString("description"),
          config.getString("code"),
          config.getString("section").toLowerCase,
          config.getString("version"),
          config.getStringList("parameter").asScala.map { param =>
            paramAccess.getParam(
              param,
              Try(config.getString("placeholder_key")).getOrElse(""),
              Try(config.getString("sample_input_key")).getOrElse(""),
              inputType
            )
          },
          // TODO remove Try when implemented for each tool
          Try(config.getObjectList("result_views").asScala.map(entry =>
          entry.unwrapped().asScala.toMap.map(a => a._1 -> a._2.toString))).getOrElse(Seq()),
          config.getStringList("forwarding.alignment").asScala,
          config.getStringList("forwarding.multi_seq").asScala,
          Try(config.getStringList("forwarding.template_alignment").asScala).toOption,
          getValidationParams(inputType, config)
        )
      case (_, _) => throw new IllegalStateException("tool does not exist")
    }
    }.toMap

  def isTool(toolName: String): Boolean = {
    toolName.toUpperCase == "REFORMAT" || toolName.toUpperCase == "ALNVIZ" || values.exists {
      case (_, tool) =>
        tool.isToolName(toolName)
      case _ => false
    }
  }

  def getValidationParams(inputType: TextAreaInputType, config: Config): ValidationParamsForm = inputType match {
    case TextAreaInputType.SEQUENCE => SequenceValidationParamsForm(
      Try(config.getStringList("sequence_restrictions.formats").asScala).getOrElse(Seq("FASTA")),
      Try(config.getString("sequence_restrictions.type")).getOrElse("PROTEIN"),
      Try(config.getInt("sequence_restrictions.min_char_per_seq")).toOption,
      Some(Try(config.getInt("sequence_restrictions.max_char_per_seq")).getOrElse(20000)),
      Try(config.getInt("sequence_restrictions.min_num_seq")).toOption,
      Some(Try(config.getInt("sequence_restrictions.max_num_seq")).getOrElse(10000)),
      Try(config.getBoolean("sequence_restrictions.same_length")).toOption,
      Try(config.getBoolean("sequence_restrictions.allow_empty")).toOption
    )
    case TextAreaInputType.REGEX => RegexValidationParamsForm(
      config.getInt("sequence_restrictions.max_regex_length")
    )
    case TextAreaInputType.ACCESSION_ID => AccessionIDValidationParamsForm(
      config.getInt("sequence_restrictions.max_num_ids")
    )
    case TextAreaInputType.PLAIN_TEXT => PlainTextValidationParamsForm(
      config.getInt("sequence_restrictions.max_num_chars")
    )
    case _ => EmptyValidationParamsForm()
  }

  private def toTool(
      toolNameShort: String,
      toolNameLong: String,
      order: Int,
      description: String,
      code: String,
      section: String,
      version: String,
      params: Seq[Parameter],
      resultViews: Seq[Map[String, String]],
      forwardAlignment: Seq[String],
      forwardMultiSeq: Seq[String],
      forwardTemplateAlignment: Option[Seq[String]],
      validationParams: ValidationParamsForm
  ): Tool = {
    val toolFormSimple = ToolFormSimple(
      toolNameShort,
      toolNameLong,
      description,
      section,
      version,
      validationParams
    )
    val inputGroup: Seq[String] = paramAccess.paramGroups("Input")
    val toolParameterForm = ToolParameters(
      Seq(
        ParameterSection(
          "Input",
          multiColumnLayout = false,
          params.filter(p => inputGroup.contains(p.name))
        ),
        ParameterSection(
          "Parameters",
          multiColumnLayout = true,
          params.filter(p => !inputGroup.contains(p.name))
        )
      ),
      ForwardingMode(
        forwardAlignment,
        forwardMultiSeq,
        forwardTemplateAlignment,
      )
    )
    Tool(
      toolNameShort,
      toolNameLong,
      order,
      description,
      code,
      section,
      toolParameterForm,
      resultViews,
      toolFormSimple
    )
  }

}
