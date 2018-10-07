package de.proteinevolution.results.controllers

import de.proteinevolution.models.forwarding.ForwardingError.InvalidModal
import de.proteinevolution.models.forwarding.{ ForwardModalOptions, ForwardingError }
import de.proteinevolution.services.ToolConfig
import javax.inject.{ Inject, Singleton }
import play.api.libs.json.Json
import play.api.mvc.{ AbstractController, Action, AnyContent, ControllerComponents }

@Singleton
class ForwardModalController @Inject()(cc: ControllerComponents, toolConfig: ToolConfig)
    extends AbstractController(cc) {

  def getForwardModalOptions(modalType: String, toolName: String): Action[AnyContent] = Action { implicit request =>
    val tool             = toolConfig.values(toolName)
    val alignmentOptions = tool.toolParameterForm.forwarding.alignment.toArray
    val multiSeqOptions  = tool.toolParameterForm.forwarding.multi_seq.toArray

    val options: Either[ForwardingError, ForwardModalOptions] = modalType match {
      case "normal" =>
        Right(
          ForwardModalOptions(
            "Forward hits",
            showRadioBtnSelection = true,
            showRadioBtnSequenceLength = true,
            alignmentOptions,
            multiSeqOptions
          )
        )
      case "hhsuite" =>
        Right(
          ForwardModalOptions(
            "Forward MSA (~100 most distinct sequences) to:",
            showRadioBtnSelection = false,
            showRadioBtnSequenceLength = false,
            Array("formatseq", "hhblits", "hhpred", "hhomp", "hhrepid"),
            Array.empty
          )
        )
      case "simple" =>
        Right(
          ForwardModalOptions(
            "Forward hits",
            showRadioBtnSelection = true,
            showRadioBtnSequenceLength = false,
            alignmentOptions,
            multiSeqOptions
          )
        )
      case "simpler" =>
        Right(
          ForwardModalOptions(
            "Forward hits",
            showRadioBtnSelection = false,
            showRadioBtnSequenceLength = false,
            alignmentOptions,
            multiSeqOptions
          )
        )
      case _ => Left(InvalidModal)
    }

    options match {
      case Right(o) => Ok(Json.toJson(o))
      case Left(e)  => BadRequest(e.message)
    }

  }

}
