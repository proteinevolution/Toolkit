package de.proteinevolution.tools

import com.typesafe.config.{ Config, ConfigObject }
import de.proteinevolution.parameters.{ ForwardingMode, Parameter, ParameterSection, ToolParameters }
import de.proteinevolution.params.ParamAccess
import de.proteinevolution.tools.forms.{ ToolFormSimple, ValidationParamsForm }
import javax.inject.{ Inject, Singleton }
import play.api.Configuration

import scala.collection.JavaConverters._

@Singleton
class ToolConfig @Inject()(config: Configuration, paramAccess: ParamAccess) {

  lazy val version: String = config.get[String]("version")

  lazy val values: Map[String, Tool] = {
    config.get[Config]("Tools").root.asScala.map {
      case (_, configObject: ConfigObject) =>
        val config = configObject.toConfig
        config.getString("name") -> toTool(
          config.getString("name"),
          config.getString("longname"),
          config.getInt("order"),
          config.getString("description"),
          config.getString("code"),
          config.getString("section").toLowerCase,
          config.getString("version"),
          config.getStringList("parameter").asScala.map { param =>
            paramAccess.getParam(param, config.getString("input_placeholder"), config.getString("sample_input_key"))
          },
          config.getStringList("forwarding.alignment").asScala,
          config.getStringList("forwarding.multi_seq").asScala
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

  private def toTool(
      toolNameShort: String,
      toolNameLong: String,
      order: Int,
      description: String,
      code: String,
      section: String,
      version: String,
      params: Seq[Parameter],
      forwardAlignment: Seq[String],
      forwardMultiSeq: Seq[String]
  ): Tool = {
    val toolFormSimple = ToolFormSimple(
      toolNameShort,
      toolNameLong,
      description,
      section,
      version,
      ValidationParamsForm(Seq("FASTA", "CLUSTAL"), "PROTEIN") // TODO
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
        forwardMultiSeq
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
      toolFormSimple
    )
  }

}
