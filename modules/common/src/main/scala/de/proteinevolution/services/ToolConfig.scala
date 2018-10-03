package de.proteinevolution.services

import com.typesafe.config.{ Config, ConfigObject }

import scala.collection.JavaConverters._
import de.proteinevolution.models.Tool
import de.proteinevolution.models.forms.{ ToolForm, ToolFormSimple, ValidationParamsForm }
import de.proteinevolution.models.param.{ Param, ParamAccess }
import javax.inject.{ Inject, Singleton}
import play.api.Configuration

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
          config.getStringList("parameter").asScala.map { param =>
            paramAccess.getParam(param, config.getString("input_placeholder"))
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
      params: Seq[Param],
      forwardAlignment: Seq[String],
      forwardMultiSeq: Seq[String]
  ): Tool = {
    val paramMap = params.map(p => p.name -> p).toMap
    val toolForm = ToolForm(
      toolNameShort,
      toolNameLong,
      code,
      section,
      paramAccess.paramGroups.keysIterator.map { group =>
        group -> paramAccess.paramGroups(group).filter(params.map(_.name).contains(_)).map(paramMap(_))
      }.toSeq :+
      "Parameters" -> params.map(_.name).diff(paramAccess.paramGroups.values.flatten.toSeq).map(paramMap(_))
    )
    val toolFormSimple = ToolFormSimple(
      toolNameShort,
      toolNameLong,
      description,
      section,
      ValidationParamsForm(Seq("FASTA", "CLUSTAL"))
    )
    Tool(
      toolNameShort,
      toolNameLong,
      order,
      description,
      code,
      section,
      paramMap,
      toolForm,
      toolFormSimple,
      paramAccess.paramGroups,
      forwardAlignment,
      forwardMultiSeq
    )
  }

}
