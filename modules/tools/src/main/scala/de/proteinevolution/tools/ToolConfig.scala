package de.proteinevolution.tools

import com.typesafe.config.{ Config, ConfigObject }
import de.proteinevolution.params.{ Param, ParamAccess }
import de.proteinevolution.tools.forms.ToolForm
import javax.inject.{ Inject, Singleton }
import play.api.Configuration

import scala.collection.JavaConverters._

@Singleton
class ToolConfig @Inject()(config: Configuration, paramAccess: ParamAccess) {

  lazy val values: Map[String, Tool] = {
    config.get[Config]("Tools").root.asScala.map {
      case (_, configObject: ConfigObject) =>
        val config = configObject.toConfig
        config.getString("name") -> toTool(
          config.getString("name"),
          config.getString("longname"),
          config.getString("code"),
          config.getString("section").toLowerCase,
          config.getStringList("parameter").asScala.map { param =>
            paramAccess.getParam(param, config.getString("input_placeholder"))
          },
          config.getStringList("forwarding.alignment").asScala,
          config.getStringList("forwarding.multi_seq").asScala,
          config.getString("title")
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
      code: String,
      category: String,
      params: Seq[Param],
      forwardAlignment: Seq[String],
      forwardMultiSeq: Seq[String],
      title: String
  ): Tool = {
    val paramMap = params.map(p => p.name -> p).toMap
    val toolForm = ToolForm(
      toolNameShort,
      toolNameLong,
      code,
      category,
      paramAccess.paramGroups.keysIterator.map { group =>
        group -> paramAccess.paramGroups(group).filter(params.map(_.name).contains(_)).map(paramMap(_))
      }.toSeq :+
      "Parameters" -> params.map(_.name).diff(paramAccess.paramGroups.values.flatten.toSeq).map(paramMap(_))
    )
    Tool(
      toolNameShort,
      toolNameLong,
      code,
      category,
      paramMap,
      toolForm,
      paramAccess.paramGroups,
      forwardAlignment,
      forwardMultiSeq,
      title
    )
  }

}
