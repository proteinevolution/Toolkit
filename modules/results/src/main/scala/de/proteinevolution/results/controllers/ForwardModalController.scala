package de.proteinevolution.results.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.models.forwarding.ForwardingError.InvalidModal
import de.proteinevolution.models.forwarding.{ ForwardModalOptions, ForwardingError }
import de.proteinevolution.tools.ToolConfig
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import io.circe.syntax._

@Singleton
class ForwardModalController @Inject()(cc: ControllerComponents, toolConfig: ToolConfig) extends ToolkitController(cc) {

  def getForwardModalOptions(modalType: String, toolName: String): Action[AnyContent] = Action { implicit request =>
    val tool             = toolConfig.values(toolName)
    val alignmentOptions = tool.forwardAlignment.toArray
    val multiSeqOptions  = tool.forwardMultiSeq.toArray

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
      case Right(o) => Ok(o.asJson)
      case Left(e)  => BadRequest(e.message)
    }

  }

}
